-- Reference copy of the Room schema (database file: insurance_bazaar.db).
-- Room creates and migrates this automatically on first launch; you do not need to run it.

CREATE TABLE customers (
    id      INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name    TEXT NOT NULL,
    phone   TEXT NOT NULL,
    email   TEXT NOT NULL,
    address TEXT NOT NULL
);

CREATE TABLE policies (
    id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    customerId    INTEGER NOT NULL,
    vehicleNumber TEXT NOT NULL,
    insurer       TEXT NOT NULL,
    policyType    TEXT NOT NULL,
    startDate     INTEGER NOT NULL,   -- UTC midnight, epoch millis
    expiryDate    INTEGER NOT NULL,   -- UTC midnight, epoch millis
    premium       REAL NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id) ON DELETE CASCADE
);
CREATE INDEX index_policies_customerId ON policies(customerId);
