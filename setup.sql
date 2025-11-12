create schema if not exists fleetforward;

set schema 'fleetforward';

\echo 'Setting up the addresses table and populating it...'
\ir 'addresses_insert.sql'
\echo 'Addresses loaded. Continuing to set up the other tables...'
\ir 'fleetforward.sql'
\echo 'Setup complete. Database is set!'