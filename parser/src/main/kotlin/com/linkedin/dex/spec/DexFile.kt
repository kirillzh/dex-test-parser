/**
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dex.spec

import java.nio.ByteBuffer
import java.nio.ByteOrder

class DexFile(byteBuffer: ByteBuffer) {
    val byteBuffer: ByteBuffer = byteBuffer.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN)
    val headerItem: HeaderItem
    val stringIds: Array<StringIdItem>
    val typeIds: Array<TypeIdItem>
    val protoIds: Array<ProtoIdItem>
    val fieldIds: Array<FieldIdItem>
    val methodIds: Array<MethodIdItem>
    val classDefs: Array<ClassDefItem>

    companion object {
        val NO_INDEX = -1
    }

    inline fun <reified T> parse(count: Int, offset: Int, size: Int, init: (ByteBuffer) -> T): Array<T> {
        return Array(count, { index ->
            byteBuffer.position(offset + (index * size))
            init(byteBuffer)
        })
    }

    init {
        this.byteBuffer.position(0)
        headerItem = HeaderItem(this.byteBuffer)
        headerItem.validate()
        stringIds = parse(headerItem.stringIdsSize, headerItem.stringIdsOff, StringIdItem.size, ::StringIdItem)
        typeIds = parse(headerItem.typeIdsSize, headerItem.typeIdsOff, TypeIdItem.size, ::TypeIdItem)
        protoIds = parse(headerItem.protoIdsSize, headerItem.protoIdsOff, ProtoIdItem.size, ::ProtoIdItem)
        fieldIds = parse(headerItem.fieldIdsSize, headerItem.fieldIdsOff, FieldIdItem.size, ::FieldIdItem)
        methodIds = parse(headerItem.methodIdsSize, headerItem.methodIdsOff, MethodIdItem.size, ::MethodIdItem)
        classDefs = parse(headerItem.classDefsSize, headerItem.classDefsOff, ClassDefItem.size, ::ClassDefItem)
    }
}
