package com.example.realmdbtest

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration


class MyMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        // DynamicRealm exposes an editable schema
        var oldVersion = oldVersion
        val schema = realm.schema

        // Migrate to version 1: Add a new class.
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 0L) {
            schema.create("Person")
                    .addField("name", String::class.java)
                    .addField("age", Int::class.javaPrimitiveType)
            oldVersion++
        }

        // Migrate to version 2: Add a primary key + object references
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     @PrimaryKey
        //     private int id;
        //     private Dog favoriteDog;
        //     private RealmList<Dog> dogs;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 1L) {
            schema["assettype"]
                    ?.addField("id", Long::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                    ?.addRealmObjectField("favoriteDog", schema["Dog"])
                    ?.addRealmListField("dogs", schema["Dog"])
            oldVersion++
        }
    }
}