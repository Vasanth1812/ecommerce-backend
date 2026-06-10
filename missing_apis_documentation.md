# FMCG E-Commerce Platform: Testing & Implementation Documentation

## 1. Quality Assurance: Positive & Negative Test Cases

### 1.1 Authentication & Authorization
**Positive Cases:**
- **TC-AUTH-01:** Admin logs in with valid credentials; verified JWT token is stored in sessionStorage and redirected to dashboard.
- **TC-AUTH-02:** User registers with valid email, phone, and password; OTP is sent and verified successfully.
- **TC-AUTH-03:** Token refresh process triggers automatically before expiry without interrupting user workflow.
- **TC-AUTH-04:** Admin accesses role-restricted routes (e.g., Settings) and successfully loads the page based on Super Admin permissions.

**Negative Cases:**
- **TC-AUTH-N01:** Admin attempts login with incorrect password; receives generic "Invalid credentials" error.
- **TC-AUTH-N02:** Expired or tampered JWT token in sessionStorage; user is immediately logged out and redirected to `/admin/login`.
- **TC-AUTH-N03:** Non-admin user attempts to access `/admin/*` routes; API returns 403 Forbidden and UI shows Access Denied.
- **TC-AUTH-N04:** API request sent without Bearer token; Interceptor handles 401 response and attempts refresh, failing gracefully if refresh token is invalid.

### 1.2 Products & Catalog Management
**Positive Cases:**
- **TC-PROD-01:** Admin creates a product with valid details (Name, SKU, Price, Category); product appears in the list immediately.
- **TC-PROD-02:** Searching products by partial SKU returns correct exact matches.
- **TC-PROD-03:** Storefront correctly displays active products, categories, and respects "out of stock" badges.

**Negative Cases:**
- **TC-PROD-N01:** Creating a product with a duplicate SKU; API returns 409 Conflict, UI displays error toast.
- **TC-PROD-N02:** Attempting to apply a negative discount percentage; form validation prevents submission.
- **TC-PROD-N03:** Uploading a product image exceeding 5MB; UI throws "File size too large" error before API request.

### 1.3 Order Lifecycle & Cart
**Positive Cases:**
- **TC-ORD-01:** Customer adds items to cart, applies valid 10% coupon, and checks out successfully. Order status is "Pending".
- **TC-ORD-02:** Admin updates order status from "Processing" to "Out for Delivery"; timeline updates and customer receives notification.
- **TC-ORD-03:** Admin initiates a partial refund for a missing item; ledger and payment gateway sync successfully.

**Negative Cases:**
- **TC-ORD-N01:** Customer applies expired coupon code; UI shows "Coupon expired or invalid".
- **TC-ORD-N02:** Customer attempts checkout when an item in the cart becomes out of stock; cart validates and flags the unavailable item.
- **TC-ORD-N03:** Payment gateway webhook fails; system safely marks order as "Payment Failed" rather than confirming it.

### 1.4 Vendor Management
**Positive Cases:**
- **TC-VEND-01:** Admin approves a pending vendor onboarding application; vendor status changes to "Active" and confirmation email is sent.
- **TC-VEND-02:** Settlement process calculates exact commission (e.g. 10%) on gross sales minus returns.
- **TC-VEND-03:** Vendor dashboard correctly restricts data access to only their own products and orders.

**Negative Cases:**
- **TC-VEND-N01:** Admin attempts to process a settlement with ₹0 or negative payable amount; action disabled.
- **TC-VEND-N02:** Vendor attempts to edit a product price below the minimum allowed margin; API rejects with 400 Bad Request.

---

## 2. API Gap Analysis & Implementation Roadmap

The following APIs are currently utilizing mock data in the frontend services and require backend implementation to achieve full feature parity.

### Menu: Dashboard (`/admin`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET | `/api/v1/admin/dashboard/overview` | Core metrics (Revenue, Orders, Customers count) |
| GET | `/api/v1/admin/dashboard/live-orders` | Real-time map/list of active orders |
| GET | `/api/v1/admin/dashboard/low-stock` | Widget for critical inventory alerts |
| GET | `/api/v1/admin/dashboard/vendor-payments` | Upcoming vendor payout queue |
| GET | `/api/v1/admin/dashboard/top-products` | Best-selling products by volume/revenue |
| GET | `/api/v1/admin/dashboard/acquisition` | User acquisition sources and metrics |

### Menu: Products (`/admin/products`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| POST | `/api/v1/admin/products/bulk-import` | CSV/XLSX product mass upload |
| POST | `/api/v1/admin/products/upload` | Product media/image uploads (S3 integration) |
| PUT | `/api/v1/admin/products/{id}/seo` | Update product meta tags and SEO fields |
| GET | `/api/v1/admin/products/{id}/audit` | Historical changes and audit logs per product |

### Menu: Inventory (`/admin/inventory`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/POST/PUT | `/api/v1/admin/inventory/warehouses` | Manage physical warehouse locations |
| POST | `/api/v1/admin/inventory/transfers` | Log stock transfers between locations |
| PUT | `/api/v1/admin/inventory/safety-stock` | Set minimum stock alerting thresholds |
| GET | `/api/v1/admin/inventory/fefo` | First-Expired-First-Out batch expiry data |
| GET | `/api/v1/admin/inventory/forecast` | AI/Statistical stock depletion forecasting |

### Menu: Orders (`/admin/orders`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET | `/api/v1/admin/orders/{id}/timeline` | Chronological order event tracking |
| POST | `/api/v1/admin/orders/{id}/assign-partner`| Link order to delivery fleet partner |
| POST | `/api/v1/admin/orders/{id}/substitute` | Handle out-of-stock item replacements |
| POST | `/api/v1/admin/orders/bulk` | Bulk status updates (e.g. marking 50 orders shipped) |

### Menu: Customers (`/admin/customers`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/POST/PUT | `/api/v1/admin/customers/segments` | Define dynamic customer groups (VIP, At-Risk) |
| GET | `/api/v1/admin/customers/analytics` | Purchase frequency and behavior data |
| GET/POST/PUT | `/api/v1/admin/customers/tickets` | Support ticket CRM management |
| GET/POST | `/api/v1/admin/customers/fraud-alerts` | High-risk behavior flagging |

### Menu: Promotions (`/admin/promotions`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/POST/PUT | `/api/v1/admin/promotions/flash-sales`| Time-bound heavy discount management |
| GET/POST/PUT | `/api/v1/admin/promotions/campaigns` | Multi-channel marketing campaign definitions |
| POST | `/api/v1/admin/promotions/push` | Trigger FCM/APNS push notifications |
| GET/POST | `/api/v1/admin/promotions/ab-tests` | A/B testing logic for pricing/banners |

### Menu: Reports (`/admin/reports`)
*Note: Basic list endpoints exist, but the following analytical/summary endpoints are missing:*
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET | `/api/v1/admin/reports/sales/summary` | Aggregated sales metrics |
| GET | `/api/v1/admin/reports/inventory` | Inventory valuation and aging |
| GET | `/api/v1/admin/reports/vendor` | Vendor SLA and performance reports |
| GET | `/api/v1/admin/reports/tax` | Comprehensive tax collection breakdown |
| GET | `/api/v1/admin/reports/gst/summary` | GST filing summary figures |
| GET | `/api/v1/admin/reports/cohorts` | User retention over time (Week 1, Week 4) |
| GET | `/api/v1/admin/reports/abandoned-carts`| Cart drop-off and recovery stats |
| GET | `/api/v1/admin/reports/revenue` | Gross vs Net margin profitability |
| GET | `/api/v1/reports/{type}/export` | Generate CSV/PDF downloads asynchronously |

### Menu: Vendors (`/admin/vendors`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/POST/PUT | `/api/v1/admin/vendors/onboarding` | KYC and application review workflow |
| GET | `/api/v1/admin/vendors/{id}/products` | List products assigned to specific vendor |
| GET | `/api/v1/admin/vendors/analytics` | Platform-wide vendor growth metrics |
| POST | `/api/v1/admin/settlements/{id}/process`| Trigger actual bank payout APIs |

### Menu: Delivery (`/admin/delivery`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET | `/api/v1/admin/delivery/live-tracking` | Real-time GPS coords for active riders |
| GET/POST | `/api/v1/admin/delivery/routes` | View and optimize zone mapping |
| GET | `/api/v1/admin/delivery/performance` | Rider KPIs (On-time, rating, drops) |
| GET | `/api/v1/admin/delivery/sla` | Delivery SLA compliance statistics |

### Menu: Notifications (`/admin/notifications`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET | `/api/v1/admin/notifications` | Admin alert feed |
| PATCH | `/api/v1/admin/notifications/mark-read`| Mark single/all as read |
| GET/PUT | `/api/v1/admin/notifications/prefs` | Configure which alerts admin receives |

### Menu: Profile (`/admin/profile`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/PATCH | `/api/v1/admin/profile` | Update personal admin details |
| POST | `/api/v1/admin/profile/password` | Password rotation |
| GET | `/api/v1/admin/profile/sessions` | View and revoke active JWT sessions |
| GET/PUT | `/api/v1/admin/profile/security` | Manage 2FA/MFA setup |

### Menu: Settings (`/admin/settings`)
| HTTP | Endpoint | Purpose |
|:---|:---|:---|
| GET/POST/PUT | `/api/v1/admin/settings/users` | Admin staff account CRUD |
| GET/POST/PUT | `/api/v1/admin/settings/roles` | RBAC permission matrix management |
| GET/PUT | `/api/v1/admin/settings/payment` | Stripe/Razorpay key configurations |
| GET/PUT | `/api/v1/admin/settings/gst` | Global tax slabs |
| GET/PUT | `/api/v1/admin/settings/theme` | Storefront branding configuration |
| GET | `/api/v1/admin/settings/audit-logs` | Global immutable security audit trail |
