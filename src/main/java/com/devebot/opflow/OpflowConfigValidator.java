package com.devebot.opflow;

import com.devebot.opflow.exception.OpflowConfigValidationException;
import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.supports.OpflowObjectTree;
import java.io.InputStream;
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

    private Schema schema;

    public OpflowConfigValidator() {
        this(OpflowConfigValidator.class.getResourceAsStream("/config-schema.json"));
    }

    public OpflowConfigValidator(InputStream schemaInputStream) {
        JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaInputStream));
        schema = SchemaLoader.load(jsonSchema);
    }

    @Override
    public Object validate(Map<String, Object> configuration) throws OpflowConfigValidationException {
        JSONObject jsonSubject = new JSONObject(new JSONTokener(OpflowJsonTool.toString(configuration)));
        
        try {
            schema.validate(jsonSubject);
        }
        catch (org.everit.json.schema.ValidationException ex) {
            return ex.getAllMessages();
        }
        catch (Exception ex) {
            throw new OpflowConfigValidationException(ex);
        }
        
        return null;
    }

    public static void main(String[] argv) throws Exception {
        Map<String, Object> data = OpflowObjectTree.buildMap()
                .put("id", 1)
                .put("fullname", "John Doe")
                .put("age", 0)
                .toMap();
        
        OpflowConfigValidator validator = new OpflowConfigValidator(OpflowConfigValidator.class.getResourceAsStream("/schema.json"));
        validator.validate(data);
    }
}
