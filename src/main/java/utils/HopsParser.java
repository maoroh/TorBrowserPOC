package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import router.NodeInfo;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HopsParser {

    private Gson _gson;
    private JsonReader _reader;

    public HopsParser(String hopsResourceFile){
        _gson = new Gson();
        _reader = new JsonReader(new InputStreamReader( getClass().getClassLoader().getResourceAsStream(hopsResourceFile)));
    }

    public static HopsParser of(String hopsResourceFile){
        return new HopsParser(hopsResourceFile);
    }

    public List<NodeInfo> parseNodes(){
        return _gson.fromJson(_reader, new TypeToken<ArrayList<NodeInfo>>(){}.getType());
    }
}
