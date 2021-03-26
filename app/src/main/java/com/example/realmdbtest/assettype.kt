package com.example.realmdbtest

import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.Date;
import org.bson.types.ObjectId;
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

open class assettype(
    @PrimaryKey var _id: ObjectId? = null,
    var __v: Long? = null,
    var active: Boolean? = null,
    var category_id: String? = null,
    var change_author: String? = null,
    var created: Date? = null,
    var description: String? = null,
    var fa_icon: String? = null,
    var modified: Date? = null,
    var name: String? = null,
    var oid: String? = null,
    var org_id: String? = null,
    var template: RealmList<assettype_template> = RealmList()
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template(
    var active: Boolean? = null,
    var card_visible: Boolean? = null,
    var datatype: String? = null,
    var details_visible: Boolean? = null,
    @Required
    var display_on_forms: RealmList<String> = RealmList(),
    var edit_visible: Boolean? = null,
    var frequency: String? = null,
    var is_timeseries: Boolean? = null,
    var key: String? = null,
    var label: assettype_template_label? = null,
    var readonly: Boolean? = null,
    var required: Boolean? = null,
    var title: assettype_template_title? = null,
    var values: RealmList<assettype_template_values> = RealmList(),
    var widget: String? = null
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_label(
    var locales: RealmList<assettype_template_label_locales> = RealmList()
): RealmObject() {}

@RealmClass(embedded = true)
open class assettype_template_label_locales(
    var locale: String? = null,
    var text: String? = null
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_title(
    var locales: RealmList<assettype_template_title_locales> = RealmList()
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_title_locales(
    var locale: String? = null,
    var text: String? = null
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_values(
    var display: assettype_template_values_display? = null,
    var value: String? = null
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_values_display(
    var locales: RealmList<assettype_template_values_display_locales> = RealmList()
): RealmObject() {}


@RealmClass(embedded = true)
open class assettype_template_values_display_locales(
    var locale: String? = null,
    var text: String? = null
): RealmObject() {}
