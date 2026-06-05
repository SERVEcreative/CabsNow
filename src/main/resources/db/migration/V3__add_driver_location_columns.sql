-- Add driver GPS columns for DBs created before location tracking
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'drivers' AND column_name = 'latitude');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE drivers ADD COLUMN latitude DOUBLE NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'drivers' AND column_name = 'longitude');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE drivers ADD COLUMN longitude DOUBLE NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
