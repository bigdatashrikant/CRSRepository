


--  @version $Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/sql/ddlgen/order_ddl.xml#1 $$Change: 885513 $

create table dcspp_token_crdt_crd (
	payment_group_id	varchar(40)	not null,
	token	varchar(40)	not null
,constraint dcspp_token_crdt_crd_p primary key (payment_group_id)
,constraint dcspp_tokencrdtcrd__f foreign key (payment_group_id) references dcspp_pay_group (payment_group_id));

commit;


