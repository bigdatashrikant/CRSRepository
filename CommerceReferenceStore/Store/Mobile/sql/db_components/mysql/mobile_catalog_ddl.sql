


--  @version $Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/sql/ddlgen/mobile_catalog_ddl.xml#1 $$Change: 883241 $

create table crs_mobile_img (
	promo_content_id	varchar(40)	not null,
	device_name	nvarchar(254)	null,
	url	nvarchar(254)	null);


create table crs_mobile_desc (
	promo_content_id	varchar(40)	not null,
	device_name	nvarchar(254)	null,
	url	nvarchar(254)	null);


create table crs_mobile_link (
	promo_content_id	varchar(40)	not null,
	device_name	nvarchar(254)	null,
	link_url	nvarchar(256)	null);

commit;


