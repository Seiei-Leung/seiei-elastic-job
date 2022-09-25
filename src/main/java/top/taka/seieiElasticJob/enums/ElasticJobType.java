package top.taka.seieiElasticJob.enums;

public enum  ElasticJobType {

    SIMPLE("SimpleJob", "简单任务类型"),
    DATAFLOW("DataflowJob", "流式任务类型"),
    SCRIPT("ScriptJob", "脚本任务类型");

    public final String value;
    public final String description;

    ElasticJobType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
