


--  @version $Id: //hosting-blueprint/B2CBlueprint/version/11.1/EStore/International/sql/ddlgen/seo_i18n_ddl.xml#1 $$Change: 875535 $

create table crs_seo_xlate (
	translation_id	varchar(40)	not null,
	title	varchar(254)	default null,
	display_name	varchar(254)	default null,
	description	varchar(254)	default null,
	keywords	varchar(254)	default null
,constraint crs_seo_xlate_pk primary key (translation_id));


create table crs_seo_seo_xlate (
	seo_tag_id	varchar(40)	not null,
	locale	varchar(40)	not null,
	translation_id	varchar(40)	not null
,constraint crs_seo_tag_xlt_pk primary key (seo_tag_id,locale)
,constraint crs_seo_tag_xlt_fk foreign key (translation_id) references crs_seo_xlate (translation_id));

create index crs_seo_tag_xlt_id on crs_seo_seo_xlate (translation_id);
commit;

