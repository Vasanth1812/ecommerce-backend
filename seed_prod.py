import psycopg2
from psycopg2 import sql

DB_URL = "postgres://billing_db_ttbe_user:cZBI0ImSjzGDeDbavDy2wP7DKxUvuuuR@dpg-d7n2gppo3t8c73eejqeg-a.singapore-postgres.render.com:5432/billing_db_ttbe?sslmode=require"
SCHEMA = "ecommerce"

def seed_database():
    print("Connecting to the database...")
    try:
        conn = psycopg2.connect(DB_URL, options=f"-c search_path={SCHEMA}")
        conn.autocommit = False
        cur = conn.cursor()
        
        # 1. Categories
        print("Inserting Categories...")
        cur.execute("""
            INSERT INTO categories (name, description, slug) VALUES 
            ('Snacks & Namkeen', 'Crispy and savory snacks', 'snacks-namkeen-test'),
            ('Beverages', 'Refreshing drinks and juices', 'beverages-test'),
            ('Dairy & Breakfast', 'Milk, butter, cheese, and morning essentials', 'dairy-breakfast-test'),
            ('Personal Care', 'Shampoos, soaps, and hygiene products', 'personal-care-test'),
            ('Home Care', 'Cleaning supplies and detergents', 'home-care-test')
            ON CONFLICT (slug) DO NOTHING RETURNING id;
        """)
        
        cur.execute("SELECT id FROM categories ORDER BY id DESC LIMIT 5;")
        category_ids = [row[0] for row in cur.fetchall()]

        # 2. Vendors
        print("Inserting Vendors...")
        cur.execute("""
            INSERT INTO vendors (business_name, contact_name, email, phone, gst_number, status, commission_rate) VALUES 
            ('Haldiram Snacks Pvt Ltd', 'Rajeev Kumar', 'vendor1@haldiram.com', '9876543210', '07AAAAA0000A1Z5', 'ACTIVE', 10.50),
            ('PepsiCo India', 'Anjali Desai', 'vendor2@pepsico.com', '9876543211', '27BBBBB1111B2Z6', 'ACTIVE', 12.00),
            ('Amul Dairy', 'Vikram Singh', 'vendor3@amul.com', '9876543212', '24CCCCC2222C3Z7', 'ACTIVE', 8.50),
            ('Hindustan Unilever', 'Neha Gupta', 'vendor4@hul.com', '9876543213', '27DDDDD3333D4Z8', 'ACTIVE', 15.00),
            ('ITC Limited', 'Rahul Sharma', 'vendor5@itc.com', '9876543214', '19EEEEE4444E5Z9', 'ACTIVE', 9.00)
            ON CONFLICT (email) DO NOTHING RETURNING id;
        """)
        
        cur.execute("SELECT id FROM vendors ORDER BY id DESC LIMIT 5;")
        vendor_ids = [row[0] for row in cur.fetchall()]

        # 3. Products
        print("Inserting Products...")
        cur.execute("""
            INSERT INTO products (title, sku, description, brand, category_id, price, mrp, cost_price, tax_rate, unit, weight, status, supplier) VALUES 
            ('Haldiram Aloo Bhujia (Test)', 'HAL-ALOO-400-T', 'Crispy potato noodles mildly spiced.', 'Haldiram', %s, 110.00, 120.00, 85.00, 12.00, 'g', 400.00, 'ACTIVE', 'Haldiram Snacks Pvt Ltd'),
            ('Pepsi Diet Can (Test)', 'PEP-DIET-330-T', 'Zero calorie cola beverage.', 'Pepsi', %s, 40.00, 45.00, 28.00, 18.00, 'ml', 330.00, 'ACTIVE', 'PepsiCo India'),
            ('Amul Butter Pasteurized (Test)', 'AMUL-BTR-500-T', 'Rich and creamy pure cow butter.', 'Amul', %s, 275.00, 285.00, 240.00, 5.00, 'g', 500.00, 'ACTIVE', 'Amul Dairy'),
            ('Dove Deep Moisture Body Wash (Test)', 'DOV-BW-800-T', 'Nourishing body wash for dry skin.', 'Dove', %s, 350.00, 400.00, 260.00, 18.00, 'ml', 800.00, 'ACTIVE', 'Hindustan Unilever'),
            ('Surf Excel Easy Wash (Test)', 'SRF-DTG-1KG-T', 'Stain removal detergent powder.', 'Surf Excel', %s, 125.00, 135.00, 95.00, 18.00, 'kg', 1.00, 'ACTIVE', 'Hindustan Unilever')
            ON CONFLICT (sku) DO NOTHING RETURNING id;
        """, (category_ids[0%len(category_ids)], category_ids[1%len(category_ids)], category_ids[2%len(category_ids)], category_ids[3%len(category_ids)], category_ids[4%len(category_ids)]))
        
        cur.execute("SELECT id FROM products ORDER BY id DESC LIMIT 5;")
        product_ids = [row[0] for row in cur.fetchall()]

        # 4. Product SEO
        print("Inserting Product SEO...")
        cur.execute("""
            INSERT INTO product_seo (product_id, meta_title, meta_description, slug, canonical_url, og_image) VALUES 
            (%s, 'Buy Haldiram Aloo Bhujia 400g Online', 'Order crispy and spicy Haldiram Aloo Bhujia 400g pack online. Fast delivery.', 'haldiram-aloo-bhujia-400g-t', 'https://fmcg.com/p/haldiram-aloo-bhujia-400g', 'https://fmcg.com/images/haldiram-aloo-bhujia-400g.jpg'),
            (%s, 'Diet Pepsi 330ml Can', 'Refreshing Diet Pepsi Zero Calorie Cola 330ml Can.', 'diet-pepsi-330ml-can-t', 'https://fmcg.com/p/diet-pepsi-330ml-can', 'https://fmcg.com/images/diet-pepsi-330ml-can.jpg'),
            (%s, 'Amul Pure Butter 500g', 'Amul Pasteurized Butter 500g pack. The taste of India.', 'amul-pure-butter-500g-t', 'https://fmcg.com/p/amul-pure-butter-500g', 'https://fmcg.com/images/amul-pure-butter-500g.jpg'),
            (%s, 'Dove Body Wash 800ml', 'Dove Deep Moisture Body Wash 800ml for smooth and glowing skin.', 'dove-deep-moisture-body-wash-800ml-t', 'https://fmcg.com/p/dove-deep-moisture-body-wash-800ml', 'https://fmcg.com/images/dove-deep-moisture-body-wash-800ml.jpg'),
            (%s, 'Surf Excel Detergent Powder 1kg', 'Surf Excel Easy Wash Detergent Powder 1kg packet for tough stains.', 'surf-excel-detergent-powder-1kg-t', 'https://fmcg.com/p/surf-excel-detergent-powder-1kg', 'https://fmcg.com/images/surf-excel-detergent-powder-1kg.jpg')
            ON CONFLICT (product_id) DO NOTHING;
        """, (product_ids[0], product_ids[1], product_ids[2], product_ids[3], product_ids[4]))

        # 5. User Addresses
        cur.execute("SELECT id FROM users LIMIT 5;")
        user_ids = [row[0] for row in cur.fetchall()]
        
        if user_ids:
            print("Inserting Addresses...")
            cur.execute("""
                INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default) VALUES 
                (%s, 'HOME', '123 Palm Grove, Sector 4', 'Near Water Tank', 'Mumbai', 'Maharashtra', '400053', true),
                (%s, 'WORK', 'Tech Park, Tower B', 'Level 2', 'Bangalore', 'Karnataka', '560037', true),
                (%s, 'HOME', 'Flat 401, Royal Gardens', '', 'Delhi', 'Delhi', '110001', true),
                (%s, 'HOME', 'Villa 15, Sunset Boulevard', '', 'Hyderabad', 'Telangana', '500034', true),
                (%s, 'WORK', 'Corporate Hub, Level 2', '', 'Pune', 'Maharashtra', '411014', true)
                ON CONFLICT DO NOTHING RETURNING id;
            """, (user_ids[0%len(user_ids)], user_ids[1%len(user_ids)], user_ids[2%len(user_ids)], user_ids[3%len(user_ids)], user_ids[4%len(user_ids)]))
            
            cur.execute("SELECT id FROM addresses ORDER BY id DESC LIMIT 5;")
            address_ids = [row[0] for row in cur.fetchall()]

            # 6. Orders
            if address_ids:
                print("Inserting Orders...")
                cur.execute("""
                    INSERT INTO orders (user_id, order_number, status, payment_status, payment_method, subtotal, delivery_fee, tax_amount, total, address_id) VALUES 
                    (%s, 'ORD-10001-T', 'DELIVERED', 'PAID', 'UPI', 150.00, 40.00, 18.00, 208.00, %s),
                    (%s, 'ORD-10002-T', 'PROCESSING', 'PAID', 'CREDIT_CARD', 315.00, 0.00, 23.00, 338.00, %s),
                    (%s, 'ORD-10003-T', 'SHIPPED', 'PENDING', 'COD', 125.00, 40.00, 18.00, 183.00, %s),
                    (%s, 'ORD-10004-T', 'CANCELLED', 'REFUNDED', 'NET_BANKING', 350.00, 0.00, 18.00, 368.00, %s),
                    (%s, 'ORD-10005-T', 'PENDING', 'PENDING', 'UPI', 385.00, 0.00, 30.00, 415.00, %s)
                    ON CONFLICT (order_number) DO NOTHING RETURNING id;
                """, (user_ids[0%len(user_ids)], address_ids[0%len(address_ids)], 
                      user_ids[1%len(user_ids)], address_ids[1%len(address_ids)], 
                      user_ids[2%len(user_ids)], address_ids[2%len(address_ids)], 
                      user_ids[3%len(user_ids)], address_ids[3%len(address_ids)], 
                      user_ids[4%len(user_ids)], address_ids[4%len(address_ids)]))
                
                cur.execute("SELECT id FROM orders ORDER BY id DESC LIMIT 5;")
                order_ids = [row[0] for row in cur.fetchall()]

                # 7. Order Items
                if order_ids and product_ids:
                    print("Inserting Order Items...")
                    cur.execute("""
                        INSERT INTO order_items (order_id, product_id, product_title, sku, qty, unit_price) VALUES 
                        (%s, %s, 'Haldiram Aloo Bhujia', 'HAL-ALOO-400-T', 1, 110.00),
                        (%s, %s, 'Diet Pepsi 330ml Can', 'PEP-DIET-330-T', 1, 40.00),
                        (%s, %s, 'Amul Butter Pasteurized', 'AMUL-BTR-500-T', 1, 275.00),
                        (%s, %s, 'Diet Pepsi 330ml Can', 'PEP-DIET-330-T', 1, 40.00),
                        (%s, %s, 'Surf Excel Easy Wash', 'SRF-DTG-1KG-T', 1, 125.00)
                        ON CONFLICT DO NOTHING;
                    """, (order_ids[0%len(order_ids)], product_ids[0], 
                          order_ids[1%len(order_ids)], product_ids[1], 
                          order_ids[2%len(order_ids)], product_ids[2], 
                          order_ids[3%len(order_ids)], product_ids[3], 
                          order_ids[4%len(order_ids)], product_ids[4]))
            
            # 8. Customer Notes (Optional)
            print("Inserting Customer Notes...")
            cur.execute("""
                INSERT INTO customer_notes (customer_id, created_by, note) VALUES
                (%s, %s, 'Frequent buyer of dairy products.'),
                (%s, %s, 'Complained about late delivery on ORD-10003.'),
                (%s, %s, 'Interested in bulk purchase of snacks.'),
                (%s, %s, 'Refunded manually for cancelled order ORD-10004.'),
                (%s, %s, 'Check if they want to subscribe to monthly detergen deliveries.')
            """, (user_ids[0%len(user_ids)], user_ids[0%len(user_ids)],
                  user_ids[1%len(user_ids)], user_ids[0%len(user_ids)],
                  user_ids[2%len(user_ids)], user_ids[0%len(user_ids)],
                  user_ids[3%len(user_ids)], user_ids[0%len(user_ids)],
                  user_ids[4%len(user_ids)], user_ids[0%len(user_ids)]))
                   
        conn.commit()
        print("Data seeded successfully!")
        
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
    seed_database()
