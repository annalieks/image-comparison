CREATE OR REPLACE FUNCTION diff_percent(i bigint) RETURNS double precision AS $$
DECLARE n bigint;
DECLARE amount bigint;
  BEGIN
    amount := 0;
    FOR n IN 1..64 LOOP
      amount := amount + ((i >> (n-1)) & 1);
    END LOOP;
    RETURN amount/CAST(64 as double precision);
  END
$$ LANGUAGE plpgsql;