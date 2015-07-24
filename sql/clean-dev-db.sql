delete from membership;
delete from factura_electronica;
delete from bussinesentity where codetype = 'NUMERO_FACTURA';
delete from subject where username <> 'admin';
delete from bussinesentity where codetype = 'CEDULA';
delete from organization;
delete from bussinesentity where codetype = 'RUC';



