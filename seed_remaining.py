import psycopg2
from datetime import datetime, timedelta
import random

DB_URL = "postgres://billing_db_ttbe_user:cZBI0ImSjzGDeDbavDy2wP7DKxUvuuuR@dpg-d7n2gppo3t8c73eejqeg-a.singapore-postgres.render.com:5432/billing_db_ttbe?sslmode=require"
SCHEMA = "ecommerce"

def seed_remaining():
    print("Connecting to the database...")
    try:
        conn = psycopg2.connect(DB_URL, options=f"-c search_path={SCHEMA}")
        conn.autocommit = False
        cur = conn.cursor()

        # Fetch foreign key relationships
        cur.execute("SELECT id FROM users LIMIT 5;")
        user_ids = [row[0] for row in cur.fetchall()]
        
        cur.execute("SELECT id FROM products LIMIT 5;")
        product_ids = [row[0] for row in cur.fetchall()]
        
        cur.execute("SELECT id FROM orders LIMIT 5;")
        order_ids = [row[0] for row in cur.fetchall()]

        cur.execute("SELECT id FROM product_seo LIMIT 5;")
        product_seo_ids = [row[0] for row in cur.fetchall()]

        if not (user_ids and product_ids and order_ids and product_seo_ids):
            print("Required foreign key data missing (users, products, orders, seo). Aborting.")
            return

        now = datetime.now()
        future = now + timedelta(days=30)
        past = now - timedelta(days=30)

        # 1. warehouses
        print("Inserting warehouses...")
        cur.execute("""
            INSERT INTO warehouses (name, type, address, is_active, lat, lng) VALUES 
            ('Mumbai Central Hub', 'MAIN', 'Andheri East, Mumbai', true, 19.1136, 72.8697),
            ('Delhi Fulfillment Center', 'REGIONAL', 'Okhla Phase 2, Delhi', true, 28.5355, 77.2639),
            ('Bangalore Storehouse', 'REGIONAL', 'Whitefield, Bangalore', true, 12.9698, 77.7499),
            ('Chennai Distribution', 'REGIONAL', 'Guindy, Chennai', true, 13.0067, 80.2206),
            ('Kolkata Transit Point', 'TRANSIT', 'Salt Lake, Kolkata', true, 22.5804, 88.4233)
            ON CONFLICT DO NOTHING
            RETURNING id;
        """)
        warehouse_ids = [row[0] for row in cur.fetchall()]
        if not warehouse_ids:
            cur.execute("SELECT id FROM warehouses LIMIT 5;")
            warehouse_ids = [row[0] for row in cur.fetchall()]

        # 2. inventory
        if warehouse_ids and product_ids:
            print("Inserting inventory...")
            for i in range(5):
                cur.execute("""
                    INSERT INTO inventory (product_id, warehouse_id, batch_number, expiry_date, safety_stock, reorder_point, qty_available, qty_reserved) VALUES 
                    (%s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT DO NOTHING;
                """, (
                    product_ids[i % len(product_ids)],
                    warehouse_ids[i % len(warehouse_ids)],
                    f'BATCH-2026-{i}',
                    future,
                    50, 100, 500, 20
                ))

        # 3. stock_movements
        if warehouse_ids and product_ids:
            print("Inserting stock_movements...")
            for i in range(5):
                cur.execute("""
                    INSERT INTO stock_movements (product_id, warehouse_id, movement_type, qty, reason, created_by) VALUES 
                    (%s, %s, %s, %s, %s, %s)
                    ON CONFLICT DO NOTHING;
                """, (
                    product_ids[i % len(product_ids)],
                    warehouse_ids[i % len(warehouse_ids)],
                    'IN' if i % 2 == 0 else 'OUT',
                    100,
                    'Initial Stocking' if i % 2 == 0 else 'Order fulfillment',
                    str(user_ids[0])
                ))

        # 4. product_images
        print("Inserting product_images...")
        for i in range(5):
            cur.execute("""
                INSERT INTO product_images (product_id, url, alt, sort_order, is_primary) VALUES 
                (%s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING;
            """, (
                product_ids[i % len(product_ids)],
                f'https://fmcg.com/images/prod-{i}.jpg',
                f'Product Image {i}',
                i,
                True
            ))

        # 5. coupons
        print("Inserting coupons...")
        cur.execute("""
            INSERT INTO coupons (code, type, discount_type, discount_value, min_order, max_uses, used_count, valid_from, valid_until, is_active) VALUES 
            ('WELCOME50_TEST', 'PUBLIC', 'PERCENTAGE', 50, 100, 1000, 50, %s, %s, true),
            ('SUMMER20_TEST', 'PUBLIC', 'PERCENTAGE', 20, 200, 500, 120, %s, %s, true),
            ('FLAT100_TEST', 'PUBLIC', 'FIXED', 100, 500, 200, 80, %s, %s, true),
            ('DIWALI_TEST', 'PUBLIC', 'PERCENTAGE', 15, 300, 1000, 300, %s, %s, true),
            ('NEWUSER_TEST', 'PUBLIC', 'FIXED', 50, 150, 1000, 400, %s, %s, true)
            ON CONFLICT (code) DO NOTHING
            RETURNING id;
        """, (past, future, past, future, past, future, past, future, past, future))
        coupon_ids = [row[0] for row in cur.fetchall()]
        if not coupon_ids:
            cur.execute("SELECT id FROM coupons LIMIT 5;")
            coupon_ids = [row[0] for row in cur.fetchall()]

        # 6. promotions
        print("Inserting promotions...")
        for i in range(5):
             cur.execute("""
                INSERT INTO promotions (name, description, type, discount_type, discount_value, min_order, max_uses, usage_count, start_date, end_date, status) VALUES 
                (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
             """, (
                 f'Promo {i}', f'Description {i}', 'STOREWIDE', 'PERCENTAGE', 10, 100, 500, 10, past, future, 'ACTIVE'
             ))

        # 7. carts
        print("Inserting carts...")
        for i in range(5):
            cur.execute("""
                INSERT INTO carts (user_id, applied_coupon_id, coupon_discount) VALUES 
                (%s, %s, %s) RETURNING id;
            """, (
                user_ids[i % len(user_ids)],
                coupon_ids[0] if coupon_ids and (i % 2 == 0) else None,
                50 if coupon_ids and (i % 2 == 0) else 0
            ))
            cart_id = cur.fetchone()[0]

            # 8. cart_items
            cur.execute("""
                INSERT INTO cart_items (cart_id, product_id, sku, qty, unit_price) VALUES 
                (%s, %s, %s, %s, %s)
            """, (
                cart_id,
                product_ids[i % len(product_ids)],
                f'SKU-CART-{i}',
                2,
                150.00
            ))

        # 9. order_status_history
        print("Inserting order_status_history...")
        for i in range(5):
            cur.execute("""
                INSERT INTO order_status_history (order_id, status, notes, changed_by) VALUES 
                (%s, %s, %s, %s)
            """, (
                order_ids[i % len(order_ids)],
                'PROCESSING',
                'Order is being packed',
                str(user_ids[0])
            ))

        # 10. loyalty_accounts
        print("Inserting loyalty_accounts...")
        for i in range(5):
            cur.execute("""
                INSERT INTO loyalty_accounts (user_id, points_balance, tier) VALUES 
                (%s, %s, %s) ON CONFLICT (user_id) DO NOTHING RETURNING id;
            """, (
                user_ids[i % len(user_ids)],
                (i + 1) * 100,
                'SILVER' if i < 3 else 'GOLD'
            ))

        # 11. loyalty_transactions
        print("Inserting loyalty_transactions...")
        for i in range(5):
            cur.execute("""
                INSERT INTO loyalty_transactions (user_id, order_id, action, points_earned, points_burned, description) VALUES 
                (%s, %s, %s, %s, %s, %s)
            """, (
                user_ids[i % len(user_ids)],
                order_ids[i % len(order_ids)],
                'EARN',
                50, 0,
                'Points earned for order'
            ))

        # 12. notifications
        print("Inserting notifications...")
        for i in range(5):
            cur.execute("""
                INSERT INTO notifications (user_id, type, title, message, reference_id, is_read) VALUES 
                (%s, %s, %s, %s, %s, %s)
            """, (
                user_ids[i % len(user_ids)],
                'ORDER_UPDATE',
                'Order Shipped',
                'Your order has been shipped successfully.',
                str(order_ids[i % len(order_ids)]),
                False
            ))

        # 13. product_seo_keywords
        print("Inserting product_seo_keywords...")
        for i in range(5):
            cur.execute("""
                INSERT INTO product_seo_keywords (product_seo_id, keyword) VALUES 
                (%s, %s)
            """, (
                product_seo_ids[i % len(product_seo_ids)],
                f'keyword-{i}'
            ))

        # 14. otp_sessions
        print("Inserting otp_sessions...")
        for i in range(5):
            cur.execute("""
                INSERT INTO otp_sessions (identifier, channel, otp_hash, attempts, used, expires_at) VALUES 
                (%s, %s, %s, %s, %s, %s)
            """, (
                f'user{i}@example.com',
                'EMAIL',
                'hash123',
                0, False,
                future
            ))

        # 15. auth_tokens
        print("Inserting auth_tokens...")
        for i in range(5):
            cur.execute("""
                INSERT INTO auth_tokens (user_id, refresh_token, device_id, revoked, expires_at) VALUES 
                (%s, %s, %s, %s, %s)
            """, (
                user_ids[i % len(user_ids)],
                f'refresh-token-xyz-{i}',
                f'device-{i}',
                False,
                future
            ))

        conn.commit()
        print("Remaining 15 tables seeded successfully!")
        
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
        print(f"An error occurred: {e}")
    finally:
        if 'cur' in locals():
            cur.close()
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    seed_remaining()
