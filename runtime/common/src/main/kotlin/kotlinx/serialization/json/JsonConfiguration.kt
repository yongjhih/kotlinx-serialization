/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json

import kotlinx.serialization.*
import kotlin.jvm.*

/**
 * The class responsible for JSON-specific customizable behaviour in [Json] format.
 *
 * Options list:
 * [encodeDefaults] specifies whether default values are encoded.
 * [strictMode] enables strict mode, which prohibits unknown keys in the JSON and non-numerical values in floating point numbers.
 * [unquoted] specifies whether keys and values should be quoted;
 * [prettyPrint] specifies whether resulting JSON should be pretty-printed.
 * [indent] specifies indent string to use with [prettyPrint] mode.
 * [useArrayPolymorphism] switches polymorphic serialization to the default array format.
 * [classDiscriminator] name of the class descriptor property in polymorphic serialization.
 */
public class JsonConfiguration(
    @JvmField internal val encodeDefaults: Boolean = true,
    @JvmField internal val strictMode: Boolean = true,
    @JvmField internal val unquoted: Boolean = false,
    @JvmField internal val prettyPrint: Boolean = false,
    @JvmField internal val indent: String = defaultIndent,
    @JvmField internal val useArrayPolymorphism: Boolean = false,
    @JvmField internal val classDiscriminator: String = defaultDiscriminator,
    @Deprecated(message = "Custom update modes are not fully supported", level = DeprecationLevel.WARNING)
    @JvmField internal val updateMode: UpdateMode = UpdateMode.OVERWRITE) {

    init {
        if (useArrayPolymorphism) require(classDiscriminator == defaultDiscriminator) {
            "Class discriminator should not be specified when array polymorphism is specified"
        }

        if (!prettyPrint) require(indent == defaultIndent) {
            "Indent should not be specified when default printing mode is used"
        }
    }

    companion object {
        @JvmStatic
        private val defaultIndent = "    "
        @JvmStatic
        private val defaultDiscriminator = "type"

        @JvmStatic
        @UnstableDefault
        public val Default = JsonConfiguration()
    }
}