INSERT INTO resort_settings (id, resort_name, phone, email, address_text, check_in_time, check_out_time, default_currency, resort_fee_per_night, tax_percentage, cancellation_window_hours, hold_duration_minutes, max_stay_nights)
VALUES (1, 'Veloura Cove', '+1-808-555-0148', 'stay@velouracove.example', 'A secluded coastal cove, Hawaiʻi', '15:00', '11:00', 'USD', 35.00, 12.50, 72, 15, 30);

INSERT INTO accommodation_types (id, slug, name, short_description, long_description, category, max_guests, max_adults, max_children, bed_configuration, room_size_sq_ft, base_nightly_rate, currency, view_type, featured, sort_order, active, check_in_time, check_out_time, hero_image) VALUES
('10000000-0000-0000-0000-000000000001','cove-king-room','Cove King Room','An intimate retreat wrapped in garden calm.','A considered sanctuary with a king bed, natural textures, and a private lanai steps from the cove gardens.','ROOM',2,2,1,'1 king bed',480,420.00,'USD','Tropical garden',TRUE,1,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1600&q=85'),
('10000000-0000-0000-0000-000000000002','ocean-terrace-room','Ocean Terrace Room','Wake to sea air from your private terrace.','An airy room overlooking the Pacific, with a generous terrace for slow mornings and golden-hour cocktails.','ROOM',3,2,1,'1 king bed or 2 queen beds',620,560.00,'USD','Ocean',TRUE,2,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1600&q=85'),
('10000000-0000-0000-0000-000000000003','horizon-suite','Horizon Suite','An expansive suite with uninterrupted ocean horizons.','A separate lounge, deep soaking tub, and wraparound balcony make this suite a serene base for longer stays.','SUITE',4,3,2,'1 king bed and 1 queen sofa bed',900,890.00,'USD','Panoramic ocean',TRUE,3,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1600&q=85'),
('10000000-0000-0000-0000-000000000004','serenity-spa-suite','Serenity Spa Suite','A restorative suite designed around private wellness.','A tranquil suite featuring a private treatment nook, steam shower, and garden-facing soaking tub.','SUITE',3,2,1,'1 king bed and 1 daybed',1050,1080.00,'USD','Garden and ocean',TRUE,4,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1600&q=85'),
('10000000-0000-0000-0000-000000000005','cliffside-villa','Cliffside Villa','Private villa living above the waterline.','A secluded two-bedroom villa with an infinity-edge plunge pool, outdoor shower, and dedicated butler service.','VILLA',5,4,2,'1 king bed and 2 twin beds',1800,1650.00,'USD','Cliffside ocean',TRUE,5,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=1600&q=85'),
('10000000-0000-0000-0000-000000000006','grand-pool-villa','Grand Pool Villa','A private estate for the ultimate cove escape.','A three-bedroom residence with a private pool, open-air dining pavilion, and attentive butler service for families or friends.','VILLA',8,6,4,'2 king beds and 2 twin beds',2900,2850.00,'USD','Oceanfront',TRUE,6,TRUE,'15:00','11:00','https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=1600&q=85');

INSERT INTO accommodation_units (accommodation_type_id, unit_code, floor_location) VALUES
('10000000-0000-0000-0000-000000000001','CK-101','Garden wing · ground floor'),('10000000-0000-0000-0000-000000000001','CK-102','Garden wing · ground floor'),('10000000-0000-0000-0000-000000000001','CK-103','Garden wing · ground floor'),('10000000-0000-0000-0000-000000000001','CK-201','Garden wing · upper floor'),('10000000-0000-0000-0000-000000000001','CK-202','Garden wing · upper floor'),('10000000-0000-0000-0000-000000000001','CK-203','Garden wing · upper floor'),
('10000000-0000-0000-0000-000000000002','OT-201','Ocean wing · second floor'),('10000000-0000-0000-0000-000000000002','OT-202','Ocean wing · second floor'),('10000000-0000-0000-0000-000000000002','OT-203','Ocean wing · second floor'),('10000000-0000-0000-0000-000000000002','OT-301','Ocean wing · third floor'),('10000000-0000-0000-0000-000000000002','OT-302','Ocean wing · third floor'),
('10000000-0000-0000-0000-000000000003','HS-201','Horizon building · second floor'),('10000000-0000-0000-0000-000000000003','HS-202','Horizon building · second floor'),('10000000-0000-0000-0000-000000000003','HS-301','Horizon building · third floor'),('10000000-0000-0000-0000-000000000003','HS-302','Horizon building · third floor'),
('10000000-0000-0000-0000-000000000004','SS-101','Wellness garden · ground floor'),('10000000-0000-0000-0000-000000000004','SS-102','Wellness garden · ground floor'),('10000000-0000-0000-0000-000000000004','SS-201','Wellness garden · upper floor'),
('10000000-0000-0000-0000-000000000005','CV-01','North cliff · private villa'),('10000000-0000-0000-0000-000000000005','CV-02','North cliff · private villa'),('10000000-0000-0000-0000-000000000005','CV-03','North cliff · private villa'),
('10000000-0000-0000-0000-000000000006','GP-01','Cove point · private estate'),('10000000-0000-0000-0000-000000000006','GP-02','Cove point · private estate');

INSERT INTO amenities (name, slug) VALUES
('Private pool','private-pool'),('Ocean view','ocean-view'),('Rain shower','rain-shower'),('Deep soaking tub','deep-soaking-tub'),
('Butler service','butler-service'),('Breakfast included','breakfast-included'),('Terrace','terrace'),('Mini bar','mini-bar'),
('Outdoor shower','outdoor-shower'),('Fireplace','fireplace'),('Lounge area','lounge-area'),('Smart TV','smart-tv'),('Espresso machine','espresso-machine');
INSERT INTO accommodation_amenities
SELECT a.id, m.id FROM accommodation_types a CROSS JOIN amenities m
WHERE (a.slug = 'cove-king-room' AND m.slug IN ('rain-shower','mini-bar','smart-tv','espresso-machine'))
   OR (a.slug = 'ocean-terrace-room' AND m.slug IN ('ocean-view','terrace','rain-shower','mini-bar','espresso-machine'))
   OR (a.slug = 'horizon-suite' AND m.slug IN ('ocean-view','terrace','deep-soaking-tub','lounge-area','espresso-machine'))
   OR (a.slug = 'serenity-spa-suite' AND m.slug IN ('ocean-view','deep-soaking-tub','rain-shower','breakfast-included','lounge-area'))
   OR (a.slug = 'cliffside-villa' AND m.slug IN ('private-pool','ocean-view','butler-service','terrace','outdoor-shower','breakfast-included','lounge-area'))
   OR (a.slug = 'grand-pool-villa' AND m.slug IN ('private-pool','ocean-view','butler-service','terrace','outdoor-shower','breakfast-included','fireplace','lounge-area','espresso-machine'));
INSERT INTO accommodation_features (accommodation_type_id, display_order, feature)
SELECT id, 1, 'Thoughtfully curated minibar' FROM accommodation_types WHERE slug IN ('cove-king-room','ocean-terrace-room');
INSERT INTO accommodation_features (accommodation_type_id, display_order, feature)
SELECT id, 2, 'Natural bath amenities' FROM accommodation_types;
INSERT INTO accommodation_features (accommodation_type_id, display_order, feature)
SELECT id, 3, 'Personalized arrival welcome' FROM accommodation_types WHERE category = 'VILLA';
INSERT INTO accommodation_images (accommodation_type_id, image_url, alt_text, image_type, display_order)
SELECT id, hero_image, name || ' hero view', 'HERO', 0 FROM accommodation_types;
INSERT INTO accommodation_images (accommodation_type_id, image_url, alt_text, image_type, display_order)
SELECT id, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1400&q=80', name || ' bedroom', 'BEDROOM', 1 FROM accommodation_types;
INSERT INTO accommodation_images (accommodation_type_id, image_url, alt_text, image_type, display_order)
SELECT id, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=80', name || ' view', 'VIEW', 2 FROM accommodation_types;

INSERT INTO seasonal_rates (accommodation_type_id, name, start_date, end_date, percentage_adjustment, priority)
VALUES (NULL, 'Holiday Season', '2026-12-20', '2027-01-06', 25.0000, 10);
INSERT INTO offers (slug, name, description, hero_image, discount_type, discount_value, minimum_nights, valid_from, valid_until, active, terms)
VALUES
('stay-longer','Stay Longer','Stay four nights and receive 15% off your room rate.','https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=80','PERCENTAGE',15,4,NULL,NULL,TRUE,'Subject to availability. Discount applies to eligible room charges only.'),
('romance-at-the-cove','Romance at the Cove','A thoughtful celebration with champagne, florals, and a private dinner.','https://images.unsplash.com/photo-1518509562904-e7ef99cdcc86?auto=format&fit=crop&w=1400&q=80','PACKAGE',0,2,NULL,NULL,TRUE,'Package inclusions are demonstration content and must be confirmed with the resort.'),
('wellness-escape','Wellness Escape','Restore your rhythm with a spa treatment, breakfast, and a private yoga session.','https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1400&q=80','PACKAGE',0,3,NULL,NULL,TRUE,'Package inclusions are demonstration content and must be confirmed with the resort.');
INSERT INTO offer_accommodation_types (offer_id, accommodation_type_id)
SELECT o.id, a.id FROM offers o CROSS JOIN accommodation_types a WHERE o.slug = 'stay-longer';

INSERT INTO experiences (slug, name, category, short_description, long_description, duration_minutes, starting_price, currency, hero_image, featured, active) VALUES
('sunset-sailing','Sunset Sailing','WATER','Sail the coast as the cove turns gold.','A private catamaran voyage with a captain, local bites, and time to swim in a quiet bay.','120',240,'USD','https://images.unsplash.com/photo-1500375592092-40eb2168fd21?auto=format&fit=crop&w=1400&q=80',TRUE,TRUE),
('private-beach-dinner','Private Beach Dinner','ROMANCE','A candlelit table on the sand.','A chef-led multi-course dinner served on a secluded stretch of beach.','180',680,'USD','https://images.unsplash.com/photo-1518509562904-e7ef99cdcc86?auto=format&fit=crop&w=1400&q=80',TRUE,TRUE),
('guided-snorkeling','Guided Snorkeling','ADVENTURE','Discover the reef with a local guide.','A small-group reef exploration with equipment and an experienced marine guide.','150',125,'USD','https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1400&q=80',FALSE,TRUE),
('couples-spa-ritual','Couple’s Spa Ritual','WELLNESS','A restorative ritual for two.','A side-by-side treatment followed by a quiet moment in the wellness garden.','90',390,'USD','https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=1400&q=80',TRUE,TRUE),
('morning-yoga','Morning Yoga','WELLNESS','Greet the day with oceanfront movement.','A gentle guided practice open to all levels on the lawn overlooking the cove.','60',45,'USD','https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1400&q=80',FALSE,TRUE),
('chefs-tasting-dinner','Chef’s Tasting Dinner','DINING','A seasonal tasting of the islands.','A multi-course menu celebrating local farms, fishers, and the season’s best ingredients.','150',225,'USD','https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1400&q=80',TRUE,TRUE),
('coastal-helicopter','Coastal Helicopter Excursion','ADVENTURE','See the coastline from a new perspective.','A private scenic flight over dramatic cliffs and remote beaches with an experienced pilot.','60',850,'USD','https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=1400&q=80',FALSE,TRUE);
INSERT INTO experience_images (experience_id, image_url, alt_text, display_order)
SELECT id, hero_image, name, 0 FROM experiences;
