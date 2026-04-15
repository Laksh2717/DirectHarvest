BEGIN;

-- =============================================================================
-- 0. CLEAN SLATE
-- =============================================================================

TRUNCATE TABLE
    ratings,
    orders,
    negotiation_events,
    negotiations,
    listing_images,
    listings,
    users
RESTART IDENTITY CASCADE;


INSERT INTO users (
    id, name, email, password, provider, role,
    street, city, state, pincode,
    enabled, average_rating, rating_count,
    created_at, updated_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- Main Farmer
(1, 'Rajesh Kumar', 'rajesh.kumar@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'FARMER',
 '14 Farm Lane', 'Nashik', 'Maharashtra', '422001',
 TRUE, 4.50, 2,
 '2024-10-01 00:01:00+00', '2024-10-01 00:01:00+00'),

-- Main Buyer
(2, 'Arjun Mehta', 'arjun.mehta@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'BUYER',
 '22 Market Street', 'Pune', 'Maharashtra', '411001',
 TRUE, NULL, 0,
 '2024-10-01 00:01:00+00', '2024-10-01 00:01:00+00'),

-- Extra Farmer 1 — Vegetables (Surat, Gujarat)
(3, 'Suresh Patel', 'suresh.patel@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'FARMER',
 '7 Vegetable Colony', 'Surat', 'Gujarat', '395001',
 TRUE, NULL, 0,
 '2026-04-01 00:01:00+00', '2026-04-01 00:01:00+00'),

-- Extra Farmer 2 — Grains (Indore, Madhya Pradesh)
(4, 'Mohan Sharma', 'mohan.sharma@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'FARMER',
 '3 Grain Market Road', 'Indore', 'Madhya Pradesh', '452001',
 TRUE, NULL, 0,
 '2026-04-01 00:01:00+00', '2026-04-01 00:01:00+00'),

-- Extra Farmer 3 — Pulses (Hyderabad, Telangana)
(5, 'Venkat Reddy', 'venkat.reddy@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'FARMER',
 '9 Pulse Lane', 'Hyderabad', 'Telangana', '500001',
 TRUE, NULL, 0,
 '2026-04-01 00:01:00+00', '2026-04-01 00:01:00+00'),

-- Extra Farmer 4 — Cash Crops (Amritsar, Punjab)
(6, 'Baldev Singh', 'baldev.singh@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'FARMER',
 '1 Sugar Mill Road', 'Amritsar', 'Punjab', '143001',
 TRUE, NULL, 0,
 '2026-04-01 00:01:00+00', '2026-04-01 00:01:00+00'),
 -- New Buyer
(7, 'Priya Nair', 'priya.nair@directharvest.com',
 '$2a$10$rTx6YAm12.yMVwMLKayYLuVmaiFpxLcey.V.2ofKgB7VQAvLiS/Ma',
 'LOCAL', 'BUYER',
 '5 Buyer Lane', 'Bengaluru', 'Karnataka', '560001',
 TRUE, NULL, 0,
 '2025-02-01 00:01:00+00', '2025-02-01 00:01:00+00');

    
INSERT INTO listings (
    id, farmer_id, crop_name,
    quantity, initial_quantity, price_per_kg,
    description,
    street, city, state, pincode,
    status, created_at, updated_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- ── Rajesh Kumar (ID 1) — main farmer listings ──────────────────────────────

-- L1: Wheat — Oct 2024 — OUT_OF_STOCK
(1, 1, 'Wheat',
 0.00, 1000.00, 25.00,
 'Premium quality wheat from Nashik region. Freshly harvested, well-cleaned and sun-dried. Suitable for flour mills and wholesale buyers.',
 '14 Farm Lane', 'Nashik', 'Maharashtra', '422001',
 'OUT_OF_STOCK', '2024-10-01 08:00:00+00', '2024-11-07 10:00:00+00'),

-- L2: Rice — Jan 2025 — INACTIVE (deactivated 3 days after creation, no orders)
(2, 1, 'Rice',
 2000.00, 2000.00, 40.00,
 'Fresh Kolam rice from Nashik. Well-polished and graded. Suitable for retail and wholesale distribution.',
 '14 Farm Lane', 'Nashik', 'Maharashtra', '422001',
 'INACTIVE', '2025-01-01 08:00:00+00', '2025-01-04 08:00:00+00'),

-- L3: Mustard — Jan 2025 — OUT_OF_STOCK (12 orders across the year)
(3, 1, 'Mustard',
 0.00, 5000.00, 60.00,
 'High-quality mustard seeds from Nashik farms. Cold-pressed grade, rich in oil content. Ideal for oil mills and spice manufacturers.',
 '14 Farm Lane', 'Nashik', 'Maharashtra', '422001',
 'OUT_OF_STOCK', '2025-01-01 10:00:00+00', '2025-12-07 10:00:00+00'),

-- L4: Chickpea — Apr 2026 — ACTIVE (1 active order, 500 kg allocated)
(4, 1, 'Chickpea',
 2500.00, 3000.00, 80.00,
 'Premium desi chickpeas from this season''s harvest. Bold size, clean and sorted. Excellent protein content. Suitable for dal mills and food processors.',
 '14 Farm Lane', 'Nashik', 'Maharashtra', '422001',
 'ACTIVE', '2026-04-01 08:00:00+00', '2026-04-07 10:00:00+00'),

-- ── Suresh Patel (ID 3) — vegetables ────────────────────────────────────────

-- L5: Tomato
(5, 3, 'Tomato',
 500.00, 500.00, 35.00,
 'Farm-fresh tomatoes from Surat. Vibrant red, firm texture. No pesticide residue. Direct from farm, ideal for restaurant chains and wholesalers.',
 '7 Vegetable Colony', 'Surat', 'Gujarat', '395001',
 'ACTIVE', '2026-04-01 09:30:00+00', '2026-04-01 09:00:00+00'),

-- L6: Onion
(6, 3, 'Onion',
 800.00, 800.00, 20.00,
 'High-quality red onions from Gujarat. Sharp flavour, excellent shelf life. Bulk available. Suitable for wholesale and retail distribution.',
 '7 Vegetable Colony', 'Surat', 'Gujarat', '395001',
 'ACTIVE', '2026-04-01 10:00:00+00', '2026-04-01 09:30:00+00'),

-- L7: Potato
(7, 3, 'Potato',
 1000.00, 1000.00, 15.00,
 'Fresh Jyoti variety potatoes from Surat. Uniformly sized, good shelf life. Ideal for restaurants, hotels, and vegetable wholesalers.',
 '7 Vegetable Colony', 'Surat', 'Gujarat', '395001',
 'ACTIVE', '2026-04-01 09:00:00+00', '2026-04-01 10:00:00+00'),

-- ── Mohan Sharma (ID 4) — grains ────────────────────────────────────────────

-- L8: Soybean
(8, 4, 'Soybean',
 2000.00, 2000.00, 45.00,
 'Non-GMO soybeans from Madhya Pradesh. High protein content, clean and graded. Ideal for oil extraction, animal feed, and soy product manufacturers.',
 '3 Grain Market Road', 'Indore', 'Madhya Pradesh', '452001',
 'ACTIVE', '2026-04-01 09:30:00+00', '2026-04-01 09:00:00+00'),

-- L9: Maize
(9, 6, 'Maize',
 3000.00, 3000.00, 18.00,
 'Yellow maize from Indore region. Dry and well-cleaned. Suitable for poultry feed, starch manufacturing, and food processing industries.',
 '3 Grain Market Road', 'Indore', 'Madhya Pradesh', '452001',
 'ACTIVE', '2026-04-01 10:00:00+00', '2026-04-01 09:30:00+00'),

-- L10: Sorghum
(10, 4, 'Sorghum',
 1500.00, 1500.00, 22.00,
 'High-quality sorghum (jowar) from Indore farms. Clean and well-sorted. Suitable for flour production, animal feed, and ethanol manufacturing.',
 '3 Grain Market Road', 'Indore', 'Madhya Pradesh', '452001',
 'ACTIVE', '2026-04-01 09:00:00+00', '2026-04-01 10:00:00+00'),

-- ── Venkat Reddy (ID 5) — pulses ────────────────────────────────────────────

-- L11: Red Lentil (Masoor)
(11, 5, 'Red Lentil',
 400.00, 400.00, 90.00,
 'Premium masoor dal from Telangana. Uniform size, excellent cooking quality. Ideal for dal mills and distributors across South India.',
 '9 Pulse Lane', 'Hyderabad', 'Telangana', '500001',
 'ACTIVE', '2026-04-01 10:00:00+00', '2026-04-01 09:00:00+00'),

-- L12: Green Gram (Moong)
(12, 5, 'Green Gram',
 600.00, 600.00, 75.00,
 'Fresh moong from Hyderabad farms. Clean and sorted. High germination rate. Suitable for dal mills, sprout manufacturers, and food processors.',
 '9 Pulse Lane', 'Hyderabad', 'Telangana', '500001',
 'ACTIVE', '2026-04-01 09:30:00+00', '2026-04-01 09:30:00+00'),

-- L13: Black Gram (Urad)
(13, 5, 'Black Gram',
 500.00, 500.00, 80.00,
 'Quality urad from Telangana. Bold and clean. Excellent for dal mills, papad manufacturers, and idli-dosa batter producers.',
 '9 Pulse Lane', 'Hyderabad', 'Telangana', '500001',
 'ACTIVE', '2026-04-01 09:00:00+00', '2026-04-01 10:00:00+00'),

-- ── Baldev Singh (ID 6) — cash crops ────────────────────────────────────────

-- L14: Sugarcane
(14, 4, 'Sugarcane',
 10000.00, 10000.00, 5.00,
 'Fresh-cut sugarcane from Punjab. High sucrose content. Suitable for jaggery units, khandsari mills, and small sugar processing units.',
 '1 Sugar Mill Road', 'Amritsar', 'Punjab', '143001',
 'ACTIVE', '2026-04-01 10:00:00+00', '2026-04-01 09:00:00+00'),

-- L15: Cotton
(15, 6, 'Cotton',
 800.00, 800.00, 65.00,
 'Long-staple cotton from Punjab farms. Ginned and cleaned. Suitable for textile mills, spinning units, and cotton yarn manufacturers.',
 '1 Sugar Mill Road', 'Amritsar', 'Punjab', '143001',
 'ACTIVE', '2026-04-01 09:30:00+00', '2026-04-01 09:30:00+00'),

(16, 6, 'Turmeric',
 200.00, 200.00, 120.00,
 'High-curcumin turmeric from Punjab. Bold rhizomes, well-dried and polished. Suitable for spice manufacturers, Ayurvedic companies, and exporters.',
 '1 Sugar Mill Road', 'Amritsar', 'Punjab', '143001',
 'ACTIVE', '2026-04-01 09:00:00+00', '2026-04-01 10:00:00+00');


INSERT INTO listing_images (listing_id, cloudinary_public_id, cloudinary_secure_url, format, width, height, bytes, is_primary, created_at) VALUES
    (16, 'directharvest/listings/dqhwyrlntbv3jgrmigvk', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776234815/directharvest/listings/dqhwyrlntbv3jgrmigvk.jpg', 'jpg', 270, 148, 10692, true, '2026-04-01 10:00:00+00'),

    (16, 'directharvest/listings/fuswonvjbwkg5osrdhty', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776234880/directharvest/listings/fuswonvjbwkg5osrdhty.jpg', 'jpg', 251, 148, 9656, false, '2026-04-01 10:00:00+00'),
    
    (16, 'directharvest/listings/jvorn9m2bvlzlmxkbwbi', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776234907/directharvest/listings/jvorn9m2bvlzlmxkbwbi.jpg', 'jpg', 270, 148, 9648, false, '2026-04-01 10:00:00+00'),
    
    (13, 'directharvest/listings/sqrrjlcbkwlucgir2kmx', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776234930/directharvest/listings/sqrrjlcbkwlucgir2kmx.jpg', 'jpg', 275, 183, 16055, true, '2026-04-01 10:00:00+00'),
    
    (13, 'directharvest/listings/r6vml0ix1yxp2bvubrwd', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776234983/directharvest/listings/r6vml0ix1yxp2bvubrwd.jpg', 'jpg', 276, 183, 13866, false, '2026-04-01 10:00:00+00'),
    
    (10, 'directharvest/listings/xk7zuglhpmzcrcnenho0', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235004/directharvest/listings/xk7zuglhpmzcrcnenho0.jpg', 'jpg', 275, 183, 16902, true, '2026-04-01 10:00:00+00'),
    
    (10, 'directharvest/listings/qunvrikcu3oxaibj10pq', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235021/directharvest/listings/qunvrikcu3oxaibj10pq.jpg', 'jpg', 259, 194, 16226, false, '2026-04-01 10:00:00+00'),
    
    (7, 'directharvest/listings/cognbxfqnktkbtuwucsn', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235046/directharvest/listings/cognbxfqnktkbtuwucsn.jpg', 'jpg', 275, 183, 10189, true, '2026-04-01 10:00:00+00'),
    
    (7, 'directharvest/listings/oxzj31l4f36hbvcj9rvl', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235063/directharvest/listings/oxzj31l4f36hbvcj9rvl.jpg', 'jpg', 275, 183, 8944, false, '2026-04-01 10:00:00+00'),
    
    (15, 'directharvest/listings/y8pa25u41gizgczdpbbk', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235077/directharvest/listings/y8pa25u41gizgczdpbbk.jpg', 'jpg', 275, 183, 11158, true, '2026-04-01 09:30:00+00'),
    
    (12, 'directharvest/listings/ohfymcvvrr81ivj2hh4n', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235096/directharvest/listings/ohfymcvvrr81ivj2hh4n.jpg', 'jpg', 275, 183, 12387, true, '2026-04-01 09:30:00+00'),
    
    (9, 'directharvest/listings/eqoejopmlhszj1bqfj8z', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235112/directharvest/listings/eqoejopmlhszj1bqfj8z.jpg', 'jpg', 612, 407, 101743, true, '2026-04-01 09:30:00+00'),
    
    (6, 'directharvest/listings/ym741woct0wzihgyem3b', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235127/directharvest/listings/ym741woct0wzihgyem3b.jpg', 'jpg', 259, 194, 16082, true, '2026-04-01 09:30:00+00'),
    
    (14, 'directharvest/listings/uml9qml1ywiat28nwl8y', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235145/directharvest/listings/uml9qml1ywiat28nwl8y.jpg', 'jpg', 276, 183, 16455, true, '2026-04-01 09:00:00+00'),
    
    (11, 'directharvest/listings/jgns886nuee06tjvghwd', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235160/directharvest/listings/jgns886nuee06tjvghwd.jpg', 'jpg', 275, 183, 8912, true, '2026-04-01 09:00:00+00'),
    
    (8, 'directharvest/listings/ydjneyrf3bktsiavt33l', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235175/directharvest/listings/ydjneyrf3bktsiavt33l.jpg', 'jpg', 300, 168, 11531, true, '2026-04-01 09:00:00+00'),
    
    (5, 'directharvest/listings/carheegpkgsxx1dt6vsw', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235191/directharvest/listings/carheegpkgsxx1dt6vsw.jpg', 'jpg', 275, 183, 16322, true, '2026-04-01 09:00:00+00'),
    
    (4, 'directharvest/listings/tqq4id31osky3mdqsyyj', 'https://res.cloudinary.com/drkcwf5i6/image/upload/v1776235209/directharvest/listings/tqq4id31osky3mdqsyyj.jpg', 'jpg', 286, 176, 13359, true, '2026-04-01 08:00:00+00');




-- Pattern for every negotiation:
--   Buyer offers price = listing_price × 0.90  (initial low offer)
--   Farmer counters    = listing_price          (full ask)
--   Buyer accepts      = listing_price          (farmer's counter accepted)
--
-- Final negotiation state therefore:
--   offered_price      = listing_price  (farmer's last counter)
--   requested_quantity = 500.00
--   status             = ACCEPTED
--   proposed_by        = FARMER         (farmer made the last proposal)
--   expires_at         = counter date + 3 days
--   updated_at         = acceptance date
--
-- L1 (Wheat  ₹25/kg) : N1 (Oct 2024), N2 (Nov 2024)
-- L3 (Mustard₹60/kg) : N3–N14 (Jan–Dec 2025, one per month)
-- L4 (Chickpea₹80/kg): N15 (Apr 2026)
-- =============================================================================

INSERT INTO negotiations (
    id, listing_id, buyer_id, farmer_id,
    offered_price, requested_quantity,
    status, proposed_by,
    expires_at, cancellation_reason, cancelled_by,
    created_at, updated_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- ── L1: Wheat (₹25/kg) ──────────────────────────────────────────────────────
-- N1 : Oct 2024
(1,  1, 2, 1, 25.00, 500.00, 'ACCEPTED', 'FARMER',
 '2024-10-09 10:00:00+00', NULL, NULL,
 '2024-10-05 10:00:00+00', '2024-10-07 10:00:00+00'),

-- N2 : Nov 2024
(2,  1, 2, 1, 25.00, 500.00, 'ACCEPTED', 'FARMER',
 '2024-11-09 10:00:00+00', NULL, NULL,
 '2024-11-05 10:00:00+00', '2024-11-07 10:00:00+00'),

-- ── L3: Mustard (₹60/kg) ────────────────────────────────────────────────────
-- N3 : Jan 2025
(3,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-01-09 10:00:00+00', NULL, NULL,
 '2025-01-05 10:00:00+00', '2025-01-07 10:00:00+00'),

-- N4 : Feb 2025
(4,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-02-09 10:00:00+00', NULL, NULL,
 '2025-02-05 10:00:00+00', '2025-02-07 10:00:00+00'),

-- N5 : Mar 2025
(5,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-03-09 10:00:00+00', NULL, NULL,
 '2025-03-05 10:00:00+00', '2025-03-07 10:00:00+00'),

-- N6 : Apr 2025  (→ order O6 gets CANCELLED by buyer within 24 h)
(6,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-04-09 10:00:00+00', NULL, NULL,
 '2025-04-05 10:00:00+00', '2025-04-07 10:00:00+00'),

-- N7 : May 2025
(7,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-05-09 10:00:00+00', NULL, NULL,
 '2025-05-05 10:00:00+00', '2025-05-07 10:00:00+00'),

-- N8 : Jun 2025
(8,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-06-09 10:00:00+00', NULL, NULL,
 '2025-06-05 10:00:00+00', '2025-06-07 10:00:00+00'),

-- N9 : Jul 2025
(9,  3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-07-09 10:00:00+00', NULL, NULL,
 '2025-07-05 10:00:00+00', '2025-07-07 10:00:00+00'),

-- N10 : Aug 2025  (→ order O10 gets CANCELLED by farmer within 24 h)
(10, 3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-08-09 10:00:00+00', NULL, NULL,
 '2025-08-05 10:00:00+00', '2025-08-07 10:00:00+00'),

-- N11 : Sep 2025
(11, 3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-09-09 10:00:00+00', NULL, NULL,
 '2025-09-05 10:00:00+00', '2025-09-07 10:00:00+00'),

-- N12 : Oct 2025
(12, 3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-10-09 10:00:00+00', NULL, NULL,
 '2025-10-05 10:00:00+00', '2025-10-07 10:00:00+00'),

-- N13 : Nov 2025
(13, 3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-11-09 10:00:00+00', NULL, NULL,
 '2025-11-05 10:00:00+00', '2025-11-07 10:00:00+00'),

-- N14 : Dec 2025
(14, 3, 2, 1, 60.00, 500.00, 'ACCEPTED', 'FARMER',
 '2025-12-09 10:00:00+00', NULL, NULL,
 '2025-12-05 10:00:00+00', '2025-12-07 10:00:00+00'),

-- ── L4: Chickpea (₹80/kg) ───────────────────────────────────────────────────
-- N15 : Apr 2026
(15, 4, 2, 1, 80.00, 500.00, 'ACCEPTED', 'FARMER',
 '2026-04-09 10:00:00+00', NULL, NULL,
 '2026-04-05 10:00:00+00', '2026-04-07 10:00:00+00'),
 -- ── L3: Mustard — REJECTED negotiations (Priya, buyer ID 7) ─────────────────
-- N16 : Apr 2025 — REJECTED by farmer
--   Flow: buyer offer → farmer counter → buyer counter → farmer rejects
--   Final state: proposed_by=BUYER (buyer's last counter), cancelled_by=FARMER
(16, 3, 7, 1, 57.00, 500.00, 'REJECTED', 'BUYER',
 '2025-04-17 10:00:00+00', 'Price still not acceptable.', 'FARMER',
 '2025-04-10 10:00:00+00', '2025-04-13 10:00:00+00'),

-- N17 : Aug 2025 — REJECTED by farmer
(17, 3, 7, 1, 57.00, 500.00, 'REJECTED', 'BUYER',
 '2025-08-17 10:00:00+00', 'Cannot agree on this price.', 'FARMER',
 '2025-08-10 10:00:00+00', '2025-08-13 10:00:00+00'),

-- ── L3: Mustard — EXPIRED negotiations (Priya, buyer ID 7) ──────────────────
-- N18 : Jun 2025 — EXPIRED
--   Flow: buyer offer → farmer counter → buyer counter → farmer ignores 3 days
--   expires_at is in the past relative to the event timestamps
(18, 3, 7, 1, 57.00, 500.00, 'EXPIRED', 'BUYER',
 '2025-06-18 10:00:00+00', NULL, NULL,
 '2025-06-11 10:00:00+00', '2025-06-18 10:00:00+00'),

-- N19 : Oct 2025 — EXPIRED
(19, 3, 7, 1, 57.00, 500.00, 'EXPIRED', 'BUYER',
 '2025-10-18 10:00:00+00', NULL, NULL,
 '2025-10-11 10:00:00+00', '2025-10-18 10:00:00+00'),

-- ── L4: Chickpea — ACTIVE negotiation (Priya, buyer ID 7) ───────────────────
-- N20 : Apr 2026 — PENDING_FARMER (just created, farmer yet to respond)
-- Note: quantity 300 kg is well within L4's available 2500 kg
(20, 4, 7, 1, 72.00, 300.00, 'PENDING_FARMER', 'BUYER',
 (NOW() - INTERVAL '1 minute') + INTERVAL '3 days', NULL, NULL,
 NOW() - INTERVAL '1 minute', NOW() - INTERVAL '1 minute');


-- =============================================================================
-- 4. NEGOTIATION EVENTS  (45 total — 3 per negotiation)
-- =============================================================================
-- Each negotiation has exactly three events in order:
--   Event 1 (CREATED)  — buyer creates initial offer at ~90% of listing price
--   Event 2 (COUNTERED)— farmer counters at full listing price
--   Event 3 (ACCEPTED) — buyer accepts farmer's counter
--
-- offered_price snapshot per listing:
--   L1  Wheat    : 22.50 (buyer) → 25.00 (farmer) → 25.00 (accepted)
--   L3  Mustard  : 54.00 (buyer) → 60.00 (farmer) → 60.00 (accepted)
--   L4  Chickpea : 72.00 (buyer) → 80.00 (farmer) → 80.00 (accepted)
--
-- status_after per event:
--   CREATED   → PENDING_FARMER
--   COUNTERED → PENDING_BUYER
--   ACCEPTED  → ACCEPTED
-- =============================================================================

INSERT INTO negotiation_events (
    id, negotiation_id,
    actor_user_id, actor_role, event_type,
    offered_price, requested_quantity, status_after,
    created_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- ── N1 (Wheat Oct 2024) ──────────────────────────────────────────────────────
(1,  1, 2, 'BUYER',  'CREATED',   22.50, 500.00, 'PENDING_FARMER', '2024-10-05 10:00:00+00'),
(2,  1, 1, 'FARMER', 'COUNTERED', 25.00, 500.00, 'PENDING_BUYER',  '2024-10-06 10:00:00+00'),
(3,  1, 2, 'BUYER',  'ACCEPTED',  25.00, 500.00, 'ACCEPTED',       '2024-10-07 10:00:00+00'),

-- ── N2 (Wheat Nov 2024) ──────────────────────────────────────────────────────
(4,  2, 2, 'BUYER',  'CREATED',   22.50, 500.00, 'PENDING_FARMER', '2024-11-05 10:00:00+00'),
(5,  2, 1, 'FARMER', 'COUNTERED', 25.00, 500.00, 'PENDING_BUYER',  '2024-11-06 10:00:00+00'),
(6,  2, 2, 'BUYER',  'ACCEPTED',  25.00, 500.00, 'ACCEPTED',       '2024-11-07 10:00:00+00'),

-- ── N3 (Mustard Jan 2025) ────────────────────────────────────────────────────
(7,  3, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-01-05 10:00:00+00'),
(8,  3, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-01-06 10:00:00+00'),
(9,  3, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-01-07 10:00:00+00'),

-- ── N4 (Mustard Feb 2025) ────────────────────────────────────────────────────
(10, 4, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-02-05 10:00:00+00'),
(11, 4, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-02-06 10:00:00+00'),
(12, 4, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-02-07 10:00:00+00'),

-- ── N5 (Mustard Mar 2025) ────────────────────────────────────────────────────
(13, 5, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-03-05 10:00:00+00'),
(14, 5, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-03-06 10:00:00+00'),
(15, 5, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-03-07 10:00:00+00'),

-- ── N6 (Mustard Apr 2025 — order will be cancelled by buyer) ─────────────────
(16, 6, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-04-05 10:00:00+00'),
(17, 6, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-04-06 10:00:00+00'),
(18, 6, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-04-07 10:00:00+00'),

-- ── N7 (Mustard May 2025) ────────────────────────────────────────────────────
(19, 7, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-05-05 10:00:00+00'),
(20, 7, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-05-06 10:00:00+00'),
(21, 7, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-05-07 10:00:00+00'),

-- ── N8 (Mustard Jun 2025) ────────────────────────────────────────────────────
(22, 8, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-06-05 10:00:00+00'),
(23, 8, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-06-06 10:00:00+00'),
(24, 8, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-06-07 10:00:00+00'),

-- ── N9 (Mustard Jul 2025) ────────────────────────────────────────────────────
(25, 9, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-07-05 10:00:00+00'),
(26, 9, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-07-06 10:00:00+00'),
(27, 9, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-07-07 10:00:00+00'),

-- ── N10 (Mustard Aug 2025 — order will be cancelled by farmer) ───────────────
(28, 10, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-08-05 10:00:00+00'),
(29, 10, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-08-06 10:00:00+00'),
(30, 10, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-08-07 10:00:00+00'),

-- ── N11 (Mustard Sep 2025) ───────────────────────────────────────────────────
(31, 11, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-09-05 10:00:00+00'),
(32, 11, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-09-06 10:00:00+00'),
(33, 11, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-09-07 10:00:00+00'),

-- ── N12 (Mustard Oct 2025) ───────────────────────────────────────────────────
(34, 12, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-10-05 10:00:00+00'),
(35, 12, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-10-06 10:00:00+00'),
(36, 12, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-10-07 10:00:00+00'),

-- ── N13 (Mustard Nov 2025) ───────────────────────────────────────────────────
(37, 13, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-11-05 10:00:00+00'),
(38, 13, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-11-06 10:00:00+00'),
(39, 13, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-11-07 10:00:00+00'),

-- ── N14 (Mustard Dec 2025) ───────────────────────────────────────────────────
(40, 14, 2, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-12-05 10:00:00+00'),
(41, 14, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-12-06 10:00:00+00'),
(42, 14, 2, 'BUYER',  'ACCEPTED',  60.00, 500.00, 'ACCEPTED',       '2025-12-07 10:00:00+00'),

-- ── N15 (Chickpea Apr 2026) ──────────────────────────────────────────────────
(43, 15, 2, 'BUYER',  'CREATED',   72.00, 500.00, 'PENDING_FARMER', '2026-04-05 10:00:00+00'),
(44, 15, 1, 'FARMER', 'COUNTERED', 80.00, 500.00, 'PENDING_BUYER',  '2026-04-06 10:00:00+00'),
(45, 15, 2, 'BUYER',  'ACCEPTED',  80.00, 500.00, 'ACCEPTED',       '2026-04-07 10:00:00+00'),

-- ── N16 (Mustard Apr 2025 — REJECTED) ────────────────────────────────────────
-- buyer offers 54 → farmer counters 60 → buyer counters 57 → farmer rejects at 57
(46, 16, 7, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-04-10 10:00:00+00'),
(47, 16, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-04-11 10:00:00+00'),
(48, 16, 7, 'BUYER',  'COUNTERED', 57.00, 500.00, 'PENDING_FARMER', '2025-04-12 10:00:00+00'),
(49, 16, 1, 'FARMER', 'REJECTED',  57.00, 500.00, 'REJECTED',       '2025-04-13 10:00:00+00'),

-- ── N17 (Mustard Aug 2025 — REJECTED) ────────────────────────────────────────
(50, 17, 7, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-08-10 10:00:00+00'),
(51, 17, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-08-11 10:00:00+00'),
(52, 17, 7, 'BUYER',  'COUNTERED', 57.00, 500.00, 'PENDING_FARMER', '2025-08-12 10:00:00+00'),
(53, 17, 1, 'FARMER', 'REJECTED',  57.00, 500.00, 'REJECTED',       '2025-08-13 10:00:00+00'),

-- ── N18 (Mustard Jun 2025 — EXPIRED) ─────────────────────────────────────────
-- buyer offers 54 → farmer counters 60 → buyer counters 57
-- farmer never responds, system expires it after 3 days (actor_user_id = NULL)
(54, 18, 7, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-06-11 10:00:00+00'),
(55, 18, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-06-12 10:00:00+00'),
(56, 18, 7, 'BUYER',  'COUNTERED', 57.00, 500.00, 'PENDING_FARMER', '2025-06-14 10:00:00+00'),
(57, 18, NULL, NULL,  'EXPIRED',   57.00, 500.00, 'EXPIRED',        '2025-06-17 10:00:00+00'),

-- ── N19 (Mustard Oct 2025 — EXPIRED) ─────────────────────────────────────────
(58, 19, 7, 'BUYER',  'CREATED',   54.00, 500.00, 'PENDING_FARMER', '2025-10-11 10:00:00+00'),
(59, 19, 1, 'FARMER', 'COUNTERED', 60.00, 500.00, 'PENDING_BUYER',  '2025-10-12 10:00:00+00'),
(60, 19, 7, 'BUYER',  'COUNTERED', 57.00, 500.00, 'PENDING_FARMER', '2025-10-14 10:00:00+00'),
(61, 19, NULL, NULL,  'EXPIRED',   57.00, 500.00, 'EXPIRED',        '2025-10-17 10:00:00+00'),

-- ── N20 (Chickpea Apr 2026 — PENDING_FARMER) ─────────────────────────────────
(62, 20, 7, 'BUYER', 'CREATED', 72.00, 300.00, 'PENDING_FARMER',
NOW() - INTERVAL '1 minute');


-- =============================================================================
-- 5. ORDERS  (15 total)
-- =============================================================================
-- agreed_price  = negotiation's final offered_price (farmer's counter)
-- agreed_quantity = 500.00 for all orders
-- created_at    = same as negotiation acceptance timestamp
-- activated_at  = created_at + 24 h  (set for all non-CANCELLED orders)
-- completed_at  = ~18 days after creation (set for COMPLETED orders)
-- cancelled orders (O6, O10): cancelled within 24 h, listing quantity restored
--
-- L1 listing quantity restoration: neither O1 nor O2 were cancelled → qty stays 0
-- L3 listing quantity restoration:
--   O6 cancelled (Apr) → 500 kg restored back to L3
--   O10 cancelled (Aug) → 500 kg restored back to L3
--   Net consumed by L3: 10 completed × 500 = 5000 kg → qty = 0 ✓
-- =============================================================================

INSERT INTO orders (
    id, listing_id, negotiation_id, buyer_id, farmer_id,
    agreed_price, agreed_quantity,
    status,
    cancelled_by, cancelled_reason, cancelled_at,
    activated_at, completed_at,
    created_at, updated_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- ── L1: Wheat orders (Oct–Nov 2024) ─────────────────────────────────────────

-- O1 : Oct 2024 — COMPLETED
(1,  1, 1,  2, 1, 25.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2024-10-08 10:00:00+00', '2024-10-25 10:00:00+00',
 '2024-10-07 10:00:00+00', '2024-10-25 10:00:00+00'),

-- O2 : Nov 2024 — COMPLETED  (rating R1 is for this order)
(2,  1, 2,  2, 1, 25.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2024-11-08 10:00:00+00', '2024-11-25 10:00:00+00',
 '2024-11-07 10:00:00+00', '2024-11-25 10:00:00+00'),

-- ── L3: Mustard orders (Jan–Dec 2025) ───────────────────────────────────────

-- O3 : Jan 2025 — COMPLETED
(3,  3, 3,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-01-08 10:00:00+00', '2025-01-25 10:00:00+00',
 '2025-01-07 10:00:00+00', '2025-01-25 10:00:00+00'),

-- O4 : Feb 2025 — COMPLETED
(4,  3, 4,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-02-08 10:00:00+00', '2025-02-25 10:00:00+00',
 '2025-02-07 10:00:00+00', '2025-02-25 10:00:00+00'),

-- O5 : Mar 2025 — COMPLETED
(5,  3, 5,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-03-08 10:00:00+00', '2025-03-25 10:00:00+00',
 '2025-03-07 10:00:00+00', '2025-03-25 10:00:00+00'),

-- O6 : Apr 2025 — CANCELLED by BUYER within 24 h (listing qty restored)
(6,  3, 6,  2, 1, 60.00, 500.00, 'CANCELLED',
 'BUYER', 'Requirements changed, cancelling order.', '2025-04-07 15:00:00+00',
 NULL, NULL,
 '2025-04-07 10:00:00+00', '2025-04-07 15:00:00+00'),

-- O7 : May 2025 — COMPLETED
(7,  3, 7,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-05-08 10:00:00+00', '2025-05-25 10:00:00+00',
 '2025-05-07 10:00:00+00', '2025-05-25 10:00:00+00'),

-- O8 : Jun 2025 — COMPLETED
(8,  3, 8,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-06-08 10:00:00+00', '2025-06-25 10:00:00+00',
 '2025-06-07 10:00:00+00', '2025-06-25 10:00:00+00'),

-- O9 : Jul 2025 — COMPLETED
(9,  3, 9,  2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-07-08 10:00:00+00', '2025-07-25 10:00:00+00',
 '2025-07-07 10:00:00+00', '2025-07-25 10:00:00+00'),

-- O10 : Aug 2025 — CANCELLED by FARMER within 24 h (listing qty restored)
(10, 3, 10, 2, 1, 60.00, 500.00, 'CANCELLED',
 'FARMER', 'Unable to fulfill due to unexpected crop damage.', '2025-08-07 15:00:00+00',
 NULL, NULL,
 '2025-08-07 10:00:00+00', '2025-08-07 15:00:00+00'),

-- O11 : Sep 2025 — COMPLETED
(11, 3, 11, 2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-09-08 10:00:00+00', '2025-09-25 10:00:00+00',
 '2025-09-07 10:00:00+00', '2025-09-25 10:00:00+00'),

-- O12 : Oct 2025 — COMPLETED
(12, 3, 12, 2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-10-08 10:00:00+00', '2025-10-25 10:00:00+00',
 '2025-10-07 10:00:00+00', '2025-10-25 10:00:00+00'),

-- O13 : Nov 2025 — COMPLETED
(13, 3, 13, 2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-11-08 10:00:00+00', '2025-11-25 10:00:00+00',
 '2025-11-07 10:00:00+00', '2025-11-25 10:00:00+00'),

-- O14 : Dec 2025 — COMPLETED  (rating R2 is for this order)
(14, 3, 14, 2, 1, 60.00, 500.00, 'COMPLETED',
 NULL, NULL, NULL,
 '2025-12-08 10:00:00+00', '2025-12-25 10:00:00+00',
 '2025-12-07 10:00:00+00', '2025-12-25 10:00:00+00'),

-- ── L4: Chickpea order (Apr 2026) ───────────────────────────────────────────

-- O15 : Apr 2026 — ACTIVE  (activated 24 h after creation, pickup pending)
(15, 4, 15, 2, 1, 80.00, 500.00, 'ACTIVE',
 NULL, NULL, NULL,
 '2026-04-08 10:00:00+00', NULL,
 '2026-04-07 10:00:00+00', '2026-04-08 10:00:00+00');


-- =============================================================================
-- 6. RATINGS  (2 total)
-- =============================================================================
-- Unique constraint: (order_id, rater_id)
-- One rating per order per rater. Allows multiple ratings for same listing if from different orders.
-- Buyer rates Farmer only (as per application flow).
--
-- R1: buyer(2) → farmer(1) for L1/Wheat via O2   — score 5 — after O2 completed
-- R2: buyer(2) → farmer(1) for L3/Mustard via O14 — score 4 — after O14 completed
--
-- Farmer average_rating = (5 + 4) / 2 = 4.50 | rating_count = 2  ✓
-- =============================================================================

INSERT INTO ratings (
    id, order_id, listing_id,
    rater_id, rated_user_id,
    score, created_at, updated_at
)
OVERRIDING SYSTEM VALUE
VALUES

-- R1 : after O2 (Wheat Nov 2024 completed)
(1, 2, 1, 2, 1, 5, '2024-11-25 12:00:00+00', '2024-11-25 12:00:00+00'),

-- R2 : after O14 (Mustard Dec 2025 completed)
(2, 14, 3, 2, 1, 4, '2025-12-25 12:00:00+00', '2025-12-25 12:00:00+00');



SELECT setval(pg_get_serial_sequence('users',               'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('listings',            'id'), (SELECT MAX(id) FROM listings));
SELECT setval(pg_get_serial_sequence('negotiations',        'id'), (SELECT MAX(id) FROM negotiations));
SELECT setval(pg_get_serial_sequence('negotiation_events',  'id'), (SELECT MAX(id) FROM negotiation_events));
SELECT setval(pg_get_serial_sequence('orders',              'id'), (SELECT MAX(id) FROM orders));
SELECT setval(pg_get_serial_sequence('ratings',             'id'), (SELECT MAX(id) FROM ratings));


COMMIT;
