package au.org.garvan.kccg.annotations.pipeline.engine.utilities;


import com.google.common.base.Strings;

public class EngineEnvironment {

    private static String workerID;
    private static Boolean selfIngestionEnabled = false;


    public static String getWorkerID(){
        if(Strings.isNullOrEmpty(workerID)){
            workerID = System.getenv("WORKER_ID")==null? "0": System.getenv("WORKER_ID");
        }
        return workerID;
    }

    public static Boolean getSelfIngestionEnabled()
    {
        String config = System.getenv("SELF_INGESTION_ENABLED");
        if(!Strings.isNullOrEmpty(config)) {
            if (config.equals("true"))
                selfIngestionEnabled = true;
            else
                selfIngestionEnabled = false;
        }

        return selfIngestionEnabled;


    }




}
