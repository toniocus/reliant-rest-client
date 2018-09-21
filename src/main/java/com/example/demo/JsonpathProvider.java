package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;

public class JsonpathProvider extends AbstractJsonProvider {

    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

    protected ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Initialize the JacksonTreeJsonProvider with the default ObjectMapper and ObjectReader
     */
    public JsonpathProvider() {
        this(defaultObjectMapper);
    }

    /**
     * Initialize the JacksonTreeJsonProvider with a custom ObjectMapper and ObjectReader.
     *
     * @param objectMapper the ObjectMapper to use
     */
    public JsonpathProvider(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object parse(final String json) throws InvalidJsonException {
        try {
            return this.objectMapper.readTree(json);
        } catch (IOException e) {
            throw new InvalidJsonException(e, json);
        }
    }

    @Override
    public Object parse(final InputStream jsonStream, final String charset) throws InvalidJsonException {
        try {
            return this.objectMapper.readTree(new InputStreamReader(jsonStream, charset));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(final Object obj) {
        if (!(obj instanceof JsonNode)) {
            throw new JsonPathException("Not a JSON Node");
        }
        return obj.toString();
    }

    @Override
    public Object createArray() {
        return JsonNodeFactory.instance.arrayNode();
    }

    @Override
    public Object createMap() {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public Object unwrap(final Object o) {

        if (o == null) {
            return null;
        }
        if (!(o instanceof JsonNode)) {
            return o;
        }

        JsonNode e = (JsonNode) o;

        if (e.isValueNode()) {

            if (e.isTextual()) {
                return e.asText();
            } else if (e.isBoolean()) {
                return e.asBoolean();
            } else if (e.isInt()) {
                return e.asInt();
            } else if (e.isLong()) {
                return e.asLong();
            } else if (e.isBigDecimal()) {
                return e.decimalValue();
            } else if (e.isDouble()) {
                return e.doubleValue();
            } else if (e.isFloat()) {
                return e.floatValue();
            } else if (e.isBigDecimal()) {
                return e.decimalValue();
            } else if (e.isNull()) {
                return null;
            }
        }
        return o;
    }


    @Override
    public boolean isArray(final Object obj) {
        return (obj instanceof ArrayNode || obj instanceof List);
    }

    @Override
    public Object getArrayIndex(final Object obj, final int idx) {
        return toJsonArray(obj).get(idx);
    }

    @Override
    public void setArrayIndex(final Object array, final int index, final Object newValue) {
        if (!isArray(array)) {
            throw new UnsupportedOperationException();
        } else {
            ArrayNode arrayNode = toJsonArray(array);
            if (index == arrayNode.size()){
                arrayNode.add(createJsonElement(newValue));
            }else {
                arrayNode.set(index, createJsonElement(newValue));
            }
        }
    }

    @Override
    public Object getMapValue(final Object obj, final String key) {
        ObjectNode jsonObject = toJsonObject(obj);
        Object o = jsonObject.get(key);
        if (!jsonObject.has(key)) {
            return UNDEFINED;
        } else {
            return unwrap(o);
        }
    }

    @Override
    public void setProperty(final Object obj, final Object key, final Object value) {
        // jlolling: Bug: #211 avoid create cloned nodes
        if (isMap(obj)) {
            setValueInObjectNode((ObjectNode) obj, key, value);
        } else {
            ArrayNode array = (ArrayNode) obj;
            int index;
            if (key != null) {
                index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            } else {
                index = array.size();
            }
            if (index == array.size()) {
                array.add(createJsonElement(value));
            } else {
                array.set(index, createJsonElement(value));
            }
        }
    }

    @Override
    public void removeProperty(final Object obj, final Object key) {
        if (isMap(obj)) {
            toJsonObject(obj).remove(key.toString());
        }
        else {
            ArrayNode array = toJsonArray(obj);
            int index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            array.remove(index);
        }
    }

    @Override
    public boolean isMap(final Object obj) {
        return (obj instanceof ObjectNode);
    }

    @Override
    public Collection<String> getPropertyKeys(final Object obj) {
        List<String> keys = new ArrayList<String>();

        Iterator<String> iter = toJsonObject(obj).fieldNames();
        while (iter.hasNext()){
            keys.add(iter.next());
        }
        return keys;
    }

    @Override
    public int length(final Object obj) {
        if (isArray(obj)) {
            return toJsonArray(obj).size();
        } else if (isMap(obj)) {
            return toJsonObject(obj).size();
        } else {
            if (obj instanceof TextNode) {
                TextNode element = (TextNode) obj;
                return element.size();
            }
        }
        throw new JsonPathException("length operation can not applied to " + obj != null ? obj.getClass().getName() : "null");
    }

    @Override
    public Iterable<?> toIterable(final Object obj) {
        ArrayNode arr = toJsonArray(obj);
        List<Object> values = new ArrayList<Object>(arr.size());
        for (Object o : arr) {
            values.add(unwrap(o));
        }
        return values;
    }

    private JsonNode createJsonElement(final Object o) {
        if (o != null) {
            // jlolling: avoid creating a cloned node: bug #211
            if (o instanceof JsonNode) {
                return (JsonNode) o;
            } else {
                return this.objectMapper.valueToTree(o);
            }
        } else {
            return null;
        }
    }

    private ArrayNode toJsonArray(final Object o) {
        return (ArrayNode) o;
    }

    private ObjectNode toJsonObject(final Object o) {
        return (ObjectNode) o;
    }

    private void setValueInObjectNode(final ObjectNode objectNode, final Object key, final Object value) {
        // jlolling: necessary to avoid deprecated methods and to avoid creating a cloned node. Bug: #211

        if (value instanceof java.util.function.Supplier) {
            setValueInObjectNode(objectNode, key, ((Supplier<?>) value).get());
        }
        else if (value instanceof JsonNode) {
            objectNode.set(key.toString(), (JsonNode) value);
        } else if (value instanceof String) {
            objectNode.put(key.toString(), (String) value);
        } else if (value instanceof Integer) {
            objectNode.put(key.toString(), (Integer) value);
        } else if (value instanceof Long) {
            objectNode.put(key.toString(), (Long) value);
        } else if (value instanceof Short) {
            objectNode.put(key.toString(), (Short) value);
        } else if (value instanceof Double) {
            objectNode.put(key.toString(), (Double) value);
        } else if (value instanceof Float) {
            objectNode.put(key.toString(), (Float) value);
        } else if (value instanceof BigDecimal) {
            objectNode.put(key.toString(), (BigDecimal) value);
        } else if (value instanceof Boolean) {
            objectNode.put(key.toString(), (Boolean) value);
        } else if (value instanceof byte[]) {
            objectNode.put(key.toString(), (byte[]) value);
        } else if (value == null) {
            objectNode.set(key.toString(), null); // this will create a null-node
        } else {
            objectNode.put(key.toString(), createJsonElement(value));
        }
    }

}
