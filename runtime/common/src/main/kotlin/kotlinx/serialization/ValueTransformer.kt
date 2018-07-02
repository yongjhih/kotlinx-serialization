/*
 * Copyright 2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.serialization

import kotlinx.serialization.CompositeDecoder.Companion.READ_ALL
import kotlin.reflect.KClass

@Suppress("OverridingDeprecatedMember")
open class ValueTransformer {
    // ------- top-level API (use it) -------

    fun <T> transform(serializer: KSerializer<T>, obj: T): T {
        val output = Output()
        output.encode(serializer, obj)
        val input = Input(output.list)
        return input.decode(serializer)
    }

    inline fun <reified T : Any> transform(obj: T): T = transform(T::class.serializer(), obj)

    // ------- override to define transformations -------

    open fun transformBooleanValue(desc: SerialDescriptor, index: Int, value: Boolean) = value
    open fun transformByteValue(desc: SerialDescriptor, index: Int, value: Byte) = value
    open fun transformShortValue(desc: SerialDescriptor, index: Int, value: Short) = value
    open fun transformIntValue(desc: SerialDescriptor, index: Int, value: Int) = value
    open fun transformLongValue(desc: SerialDescriptor, index: Int, value: Long) = value
    open fun transformFloatValue(desc: SerialDescriptor, index: Int, value: Float) = value
    open fun transformDoubleValue(desc: SerialDescriptor, index: Int, value: Double) = value
    open fun transformCharValue(desc: SerialDescriptor, index: Int, value: Char) = value
    open fun transformStringValue(desc: SerialDescriptor, index: Int, value: String) = value

    @Deprecated("Not supported in Native", replaceWith = ReplaceWith("transformEnumValue(enumLoader)"))
    open fun <T : Enum<T>> transformEnumValue(desc: SerialDescriptor, index: Int, enumClass: KClass<T>, value: T): T = value

    open fun <T : Enum<T>> transformEnumValue(desc: SerialDescriptor, index: Int, enumCreator: EnumCreator<T>, value: T): T = value

    open fun isRecursiveTransform(): Boolean = true

    // ---------------

    private inner class Output : CompositeEncoder {
        override var context: SerialContext? = null

        internal val list = arrayListOf<Any?>()

        override fun encodeNullableValue(value: Any?) {
            list.add(value)
        }

        override fun encodeElement(desc: SerialDescriptor, index: Int) = true
        override fun encodeNotNullMark() {}
        override fun encodeNull() { encodeNullableValue(null) }
        override fun encodeNonSerializableValue(value: Any) { encodeNullableValue(value) }
        override fun encodeUnit() { encodeNullableValue(Unit) }
        override fun encodeBoolean(value: Boolean) { encodeNullableValue(value) }
        override fun encodeByte(value: Byte) { encodeNullableValue(value) }
        override fun encodeShort(value: Short) { encodeNullableValue(value) }
        override fun encodeInt(value: Int) { encodeNullableValue(value) }
        override fun encodeLong(value: Long) { encodeNullableValue(value) }
        override fun encodeFloat(value: Float) { encodeNullableValue(value) }
        override fun encodeDouble(value: Double) { encodeNullableValue(value) }
        override fun encodeChar(value: Char) { encodeNullableValue(value) }
        override fun encodeString(value: String) { encodeNullableValue(value) }
        override fun <T : Enum<T>> encodeEnum(enumClass: KClass<T>, value: T) { encodeNullableValue(value) }
        override fun <T : Enum<T>> encodeEnum(value: T) {
            encodeNullableValue(value)
        }

        override fun <T : Any?> encodeSerializableValue(saver: SerializationStrategy<T>, value: T) {
            if (isRecursiveTransform()) {
                saver.serialize(this, value)
            } else
                encodeNullableValue(value)
        }

        // ---------------

        override fun encodeNonSerializableElement(desc: SerialDescriptor, index: Int, value: Any) { encodeNullableValue(value) }
        override fun encodeNullableElementValue(desc: SerialDescriptor, index: Int, value: Any?) = encodeNullableValue(value)
        override fun encodeUnitElement(desc: SerialDescriptor, index: Int) = encodeNullableValue(Unit)
        override fun encodeBooleanElement(desc: SerialDescriptor, index: Int, value: Boolean) = encodeNullableValue(value)
        override fun encodeByteElement(desc: SerialDescriptor, index: Int, value: Byte) = encodeNullableValue(value)
        override fun encodeShortElement(desc: SerialDescriptor, index: Int, value: Short) = encodeNullableValue(value)
        override fun encodeIntElement(desc: SerialDescriptor, index: Int, value: Int) = encodeNullableValue(value)
        override fun encodeLongElement(desc: SerialDescriptor, index: Int, value: Long) = encodeNullableValue(value)
        override fun encodeFloatElement(desc: SerialDescriptor, index: Int, value: Float) = encodeNullableValue(value)
        override fun encodeDoubleElement(desc: SerialDescriptor, index: Int, value: Double) = encodeNullableValue(value)
        override fun encodeCharElement(desc: SerialDescriptor, index: Int, value: Char) = encodeNullableValue(value)
        override fun encodeStringElement(desc: SerialDescriptor, index: Int, value: String) = encodeNullableValue(value)

        override fun <T : Enum<T>> encodeEnumElement(desc: SerialDescriptor, index: Int, enumClass: KClass<T>, value: T) =
            encodeNullableValue(value)

        override fun <T : Enum<T>> encodeEnumElement(desc: SerialDescriptor, index: Int, value: T) {
            encodeNullableValue(value)
        }
    }

    private inner class Input(private val list: List<Any?>) : CompositeDecoder {
        override var context: SerialContext? = null
        override val updateMode: UpdateMode = UpdateMode.BANNED

        private var index = 0
        private var curDesc: SerialDescriptor? = null
        private var curIndex: Int = 0

        private fun cur(desc: SerialDescriptor, index: Int) {
            curDesc = desc
            curIndex = index
        }

        override fun decodeNotNullMark(): Boolean = list[index] != null
        override fun decodeNull(): Nothing? { index++; return null }
        override fun decodeValue(): Any = list[index++]!!
        override fun decodeNullableValue(): Any? = list[index++]
        override fun decodeUnit(): Unit { index++ }

        override fun decodeBoolean(): Boolean = transformBooleanValue(curDesc!!, curIndex, decodeValue() as Boolean)
        override fun decodeByte(): Byte = transformByteValue(curDesc!!, curIndex, decodeValue() as Byte)
        override fun decodeShort(): Short = transformShortValue(curDesc!!, curIndex, decodeValue() as Short)
        override fun decodeInt(): Int = transformIntValue(curDesc!!, curIndex, decodeValue() as Int)
        override fun decodeLong(): Long = transformLongValue(curDesc!!, curIndex, decodeValue() as Long)
        override fun decodeFloat(): Float = transformFloatValue(curDesc!!, curIndex, decodeValue() as Float)
        override fun decodeDouble(): Double = transformDoubleValue(curDesc!!, curIndex, decodeValue() as Double)
        override fun decodeChar(): Char = transformCharValue(curDesc!!, curIndex, decodeValue() as Char)
        override fun decodeString(): String = transformStringValue(curDesc!!, curIndex, decodeValue() as String)

        @Suppress("UNCHECKED_CAST")
        @Deprecated("Not supported in Native", replaceWith = ReplaceWith("decodeEnum(enumLoader)"))
        override fun <T : Enum<T>> decodeEnum(enumClass: KClass<T>): T =
            transformEnumValue(curDesc!!, curIndex, enumClass, decodeValue() as T)

        @Suppress("UNCHECKED_CAST")
        override fun <T : Enum<T>> decodeEnum(enumCreator: EnumCreator<T>): T =
            transformEnumValue(curDesc!!, curIndex, enumCreator, decodeValue() as T)

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> decodeSerializableValue(loader: DeserializationStrategy<T>): T {
            if (isRecursiveTransform())
                return loader.deserialize(this)
            else
                return decodeValue() as T
        }

        // ---------------

        override fun decodeElementIndex(desc: SerialDescriptor): Int = READ_ALL

        override fun decodeElementValue(desc: SerialDescriptor, index: Int): Any {
            cur(desc, index); return decodeValue()
        }

        override fun decodeNullableElementValue(desc: SerialDescriptor, index: Int): Any? {
            cur(desc, index); return decodeNullableValue()
        }
        override fun decodeUnitElement(desc: SerialDescriptor, index: Int) { cur(desc, index); return decodeUnit() }
        override fun decodeBooleanElement(desc: SerialDescriptor, index: Int): Boolean { cur(desc, index); return decodeBoolean() }
        override fun decodeByteElement(desc: SerialDescriptor, index: Int): Byte { cur(desc, index); return decodeByte() }
        override fun decodeShortElement(desc: SerialDescriptor, index: Int): Short { cur(desc, index); return decodeShort() }
        override fun decodeIntElement(desc: SerialDescriptor, index: Int): Int { cur(desc, index); return decodeInt() }
        override fun decodeLongElement(desc: SerialDescriptor, index: Int): Long { cur(desc, index); return decodeLong() }
        override fun decodeFloatElement(desc: SerialDescriptor, index: Int): Float { cur(desc, index); return decodeFloat() }
        override fun decodeDoubleElement(desc: SerialDescriptor, index: Int): Double { cur(desc, index); return decodeDouble() }
        override fun decodeCharElement(desc: SerialDescriptor, index: Int): Char { cur(desc, index); return decodeChar() }
        override fun decodeStringElement(desc: SerialDescriptor, index: Int): String { cur(desc, index); return decodeString() }

        override fun <T : Enum<T>> decodeEnumElementValue(desc: SerialDescriptor, index: Int, enumClass: KClass<T>): T {
            cur(desc, index)
            return decodeEnum(enumClass)
        }

        override fun <T : Enum<T>> decodeEnumElementValue(desc: SerialDescriptor, index: Int, enumCreator: EnumCreator<T>): T {
            cur(desc, index)
            return decodeEnum(enumCreator)
        }

        override fun <T: Any?> decodeSerializableElement(desc: SerialDescriptor, index: Int, loader: DeserializationStrategy<T>): T {
            cur(desc, index)
            return decodeSerializableValue(loader)
        }

        override fun <T: Any> decodeNullableSerializableElement(desc: SerialDescriptor, index: Int, loader: DeserializationStrategy<T?>): T? {
            cur(desc, index)
            return decodeNullableSerializableValue(loader)
        }
    }
}