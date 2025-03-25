package com.mauleco.todo.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Todo: RealmObject {
    @PrimaryKey var _id: ObjectId = ObjectId()
    var itemNumber: Int = 0
    var note: String = ""
    var status: Boolean? = null
    var completionTime: Double? = null
}