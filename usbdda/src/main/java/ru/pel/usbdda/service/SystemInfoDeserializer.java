package ru.pel.usbdda.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.pel.usbdda.entity.OSInfo;
import ru.pel.usbdda.entity.SystemInfo;

import java.io.IOException;

public class SystemInfoDeserializer extends StdDeserializer<SystemInfo> {
    protected SystemInfoDeserializer(Class<?> vc) {
        super(vc);
    }
    public SystemInfoDeserializer(){
        this(null);
    }

    @Override
    public SystemInfo deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        SystemInfo systemInfo =new SystemInfo();
        ObjectCodec codec = jsonParser.getCodec();
        TreeNode systemInfoNode = codec.readTree(jsonParser);
        SystemInfo systemInfo1 = codec.readValue(jsonParser, SystemInfo.class);

//        systemInfoNode.
        TreeNode osInfoNode = systemInfoNode.get("osInfo");

        int usbDeviceMap = systemInfoNode.get("usbDeviceMap").size();
        return systemInfo;
    }
}
