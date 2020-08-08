package com.devebot.opflow;

import com.devebot.opflow.exception.OpflowConfigValidationException;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author acegik
 */
public class OpflowConfigValidator implements OpflowConfig.Validator {

    private final List<Schema> schemas = new LinkedList<>();

    public static OpflowConfigValidator getCommanderConfigValidator(InputStream ... schemaInputStreams) {
        return new OpflowConfigValidator(mergeInputStreamArray("/opflow-commander-schema.json", schemaInputStreams));
    }

    public static OpflowConfigValidator getServerletConfigValidator(InputStream ... schemaInputStreams) {
        return new OpflowConfigValidator(mergeInputStreamArray("/opflow-serverlet-schema.json", schemaInputStreams));
    }

    public static OpflowConfigValidator getUserDefinedConfigValidator(InputStream ... schemaInputStreams) {
        return new OpflowConfigValidator(schemaInputStreams);
    }

    public OpflowConfigValidator(InputStream ... schemaInputStreams) {
        for (InputStream schemaInputStream: schemaInputStreams) {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaInputStream));
            schemas.add(SchemaLoader.load(jsonSchema));
        }
    }

    @Override
    public Object validate(Map<String, Object> configuration) throws OpflowConfigValidationException {
        JSONObject jsonSubject = new JSONObject(new JSONTokener(OpflowJsonTool.toString(configuration)));

        List<String> allMessages = new LinkedList<>();
        for (Schema schema: schemas) {
            try {
                schema.validate(jsonSubject);
            }
            catch (org.everit.json.schema.ValidationException ex) {
                allMessages.addAll(ex.getAllMessages());
            }
            catch (Exception ex) {
                throw new OpflowConfigValidationException(ex);
            }
        }
        
        if (allMessages.isEmpty()) {
            return null;
        }

        return allMessages;
    }

    private static InputStream[] mergeInputStreamArray(String schemaFile, InputStream ... schemaInputStreams) {
        InputStream[] newargs = new InputStream[schemaInputStreams.length + 1];
        newargs[0] = OpflowConfigValidator.class.getResourceAsStream(schemaFile);
        System.arraycopy(schemaInputStreams, 0, newargs, 1, schemaInputStreams.length);
        return newargs;
    }

    public static void main(String[] argv) throws Exception {
        Map<String, Object> data = OpflowObjectTree.buildMap()
                .put("id", 1)
                .put("fullname", "John Doe")
                .put("age", 0)
                .toMap();
        
        OpflowConfigValidator validator = new OpflowConfigValidator(OpflowConfigValidator.class.getResourceAsStream("/schema.json"));
        Object reasons = validator.validate(data);
        if (reasons instanceof List) {
            for (Object reason: (List) reasons) {
                System.out.println("[-] " + reason.toString());
            }
        }
    }
}
