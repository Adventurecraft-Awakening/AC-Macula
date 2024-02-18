package net.mine_diver.macula.sources;

public class InternalShaderpackSource extends ShaderpackSource {

    public static final InternalShaderpackSource instance = new InternalShaderpackSource();

    @Override
    public String getName() {
        return "(internal)";
    }
}
