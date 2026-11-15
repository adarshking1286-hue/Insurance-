-- Demo data for Insurance Bazaar. Dates are relative to the day you run this.
-- Run against an existing insurance_bazaar.db (tables are created by the app on first launch).
-- Dates are UTC-midnight epoch milliseconds, like the app stores them.

INSERT INTO customers (name, phone, email, address) VALUES
  ('Rahul Sharma', '9876543210', 'rahul.sharma@example.com', 'Pune, Maharashtra'),
  ('Priya Verma',  '9811122233', 'priya.verma@example.com',  'Delhi'),
  ('Amit Patel',   '9922334455', '',                         'Ahmedabad, Gujarat'),
  ('Sneha Iyer',   '9845012345', 'sneha.iyer@example.com',   'Bengaluru, Karnataka');

INSERT INTO policies (customerId, vehicleNumber, insurer, policyType, startDate, expiryDate, premium) VALUES
  ((SELECT id FROM customers WHERE phone='9876543210'), 'MH12AB1234', 'HDFC Ergo',           'Motor - Comprehensive',
     CAST(strftime('%s','now','start of day','-165 days') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','+200 days') AS INTEGER) * 1000, 12450),
  ((SELECT id FROM customers WHERE phone='9876543210'), '',           'Star Health',         'Health',
     CAST(strftime('%s','now','start of day','-90 days') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','+275 days') AS INTEGER) * 1000, 18200),
  ((SELECT id FROM customers WHERE phone='9811122233'), 'DL8CAF5031', 'ICICI Lombard',       'Motor - Comprehensive',
     CAST(strftime('%s','now','start of day','-353 days') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','+12 days') AS INTEGER) * 1000, 9800),
  ((SELECT id FROM customers WHERE phone='9922334455'), 'GJ01KP7788', 'Bajaj Allianz',       'Motor - Third Party',
     CAST(strftime('%s','now','start of day','-385 days') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','-20 days') AS INTEGER) * 1000, 2900),
  ((SELECT id FROM customers WHERE phone='9922334455'), '',           'LIC',                 'Life',
     CAST(strftime('%s','now','start of day','-400 days') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','+330 days') AS INTEGER) * 1000, 25000),
  ((SELECT id FROM customers WHERE phone='9845012345'), 'KA05MN2201', 'New India Assurance', 'Motor - Comprehensive',
     CAST(strftime('%s','now','start of day') AS INTEGER) * 1000,
     CAST(strftime('%s','now','start of day','+365 days') AS INTEGER) * 1000, 11300);
