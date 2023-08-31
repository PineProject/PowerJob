package tech.powerjob.common.response;

import tech.powerjob.common.PowerSerializable;
import tech.powerjob.common.serialize.JsonUtils;
import lombok.*;

import java.nio.charset.StandardCharsets;


/**
 * Response to Pattens.ask
 *
 * @author tjq
 * @since 2020/3/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskResponse implements PowerSerializable {

    private boolean success;

    /*
    - Using Object will report an error: java.lang.ClassCastException: scala.collection.immutable.HashMap cannot be cast to XXX, you can only serialize and deserialize it yourself
    - Nested types (such as Map<String, B>), if B is also a complex object, then the type of B after deserialization is LinkedHashMap... It is troublesome to process (convert to JSON and then back)
    - Considering multilingual communication, data must be serialized as a byte array using JSON
     */
    private byte[] data;

    // error message
    private String message;

    public static AskResponse succeed(Object data) {
        AskResponse r = new AskResponse();
        r.success = true;
        if (data != null) {
            if (data instanceof String) {
                r.data = ((String) data).getBytes(StandardCharsets.UTF_8);
            } else {
                r.data = JsonUtils.toBytes(data);
            }
        }
        return r;
    }

    public static AskResponse failed(String msg) {
        AskResponse r = new AskResponse();
        r.success = false;
        r.message = msg;
        return r;
    }

    public <T> T getData(Class<T> clz) throws Exception {
        return JsonUtils.parseObject(data, clz);
    }

    public String parseDataAsString() {
        return new String(data, StandardCharsets.UTF_8);
    }

}
