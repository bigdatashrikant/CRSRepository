


--  @version $Id: //hosting-blueprint/B2CBlueprint/version/11.1/EStore/sql/ddlgen/multisite_ddl.xml#1 $$Change: 875535 $

create table crs_site_attribute (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	resource_bundle	varchar(254)	null,
	prod_threshold	integer	null,
	page_size	integer	null,
	css_file	varchar(254)	null,
	large_site_icon	varchar(254)	null,
	default_country_code	varchar(2)	null,
	emailafriend	tinyint	null,
	backinstock_addr	varchar(254)	null,
	newpass_addr	varchar(254)	null,
	orderconfirm_addr	varchar(254)	null,
	ordershipped_addr	varchar(254)	null,
	orderreturn_addr	varchar(254)	null,
	changepass_addr	varchar(254)	null,
	registereduser_addr	varchar(254)	null,
	promo_addr	varchar(254)	null,
	price_slider_min	integer	null,
	price_slider_max	integer	null,
	channel	varchar(254)	null
,constraint crs_site_attr_p primary key (id,asset_version))


create table crs_bill_codes (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	country_codes	varchar(40)	not null,
	sequence_num	integer	not null
,constraint crs_bill_codes_p primary key (id,sequence_num,asset_version))


create table crs_non_bill_codes (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	country_codes	varchar(40)	not null,
	sequence_num	integer	not null
,constraint crs_non_bill_c_p primary key (id,sequence_num,asset_version))


create table crs_ship_codes (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	country_codes	varchar(40)	not null,
	sequence_num	integer	not null
,constraint crs_ship_codes_p primary key (id,sequence_num,asset_version))


create table crs_non_ship_codes (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	country_codes	varchar(40)	not null,
	sequence_num	integer	not null
,constraint crs_non_ship_c_p primary key (id,sequence_num,asset_version))


create table crs_site_languages (
	asset_version	numeric(19)	not null,
	id	varchar(40)	not null,
	languages	varchar(40)	not null,
	sequence_num	integer	not null
,constraint crs_site_lang_p primary key (id,sequence_num,asset_version))



go
