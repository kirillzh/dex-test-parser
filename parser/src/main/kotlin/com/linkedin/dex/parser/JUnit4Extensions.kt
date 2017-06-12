/**
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dex.parser

import com.linkedin.dex.spec.AnnotationItem
import com.linkedin.dex.spec.AnnotationSetItem
import com.linkedin.dex.spec.AnnotationsDirectoryItem
import com.linkedin.dex.spec.ClassDefItem
import com.linkedin.dex.spec.DexFile

private val TEST_ANNOTATION_NAME = "org.junit.Test"

/**
 * Find all methods that are annotated with JUnit4's @Test annotation
 */
fun DexFile.findJUnit4Tests(): List<TestMethod> {
    val methods = mutableListOf<TestMethod>()

    classDefs.filter(::hasAnnotations).forEach { classDefItem ->
        val className = formatClassDefItemName(classDefItem)
        // iterate over each method in class def item
        val annotations = AnnotationsDirectoryItem.create(byteBuffer, classDefItem.annotationsOff).methodAnnotations
        annotations.forEach { annotation ->
            val setItem = AnnotationSetItem.create(byteBuffer, annotation.annotationsOff)
            val entries = setItem.entries.map { entry ->
                val item = AnnotationItem.create(byteBuffer, entry.annotationOff)
                formatClassName(ParseUtils.parseDescriptor(byteBuffer, typeIds[item.encodedAnnotation.typeIdx], stringIds))
            }
            if (entries.contains(TEST_ANNOTATION_NAME)) {
                val methodId = methodIds[annotation.methodIdx]
                val methodName = ParseUtils.parseMethodName(byteBuffer, stringIds, methodId)
                methods.add(TestMethod(className, methodName, entries))
            }
        }
    }

    return methods
}

private fun hasAnnotations(classDefItem: ClassDefItem): Boolean {
    return classDefItem.annotationsOff != 0
}

fun DexFile.formatClassDefItemName(classDefItem: ClassDefItem): String {
    return formatClassName(ParseUtils.parseClassName(byteBuffer, classDefItem, typeIds, stringIds))
}

fun formatClassName(className: String): String {
    return className.substring(1) // strip off the "L" prefix
            .replace('/', '.')
            .dropLast(1) // strip off the ";"
}

