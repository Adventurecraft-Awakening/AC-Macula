package net.mine_diver.macula.sources;

public class NullShaderpackSource extends ShaderpackSource {

    public static final NullShaderpackSource instance = new NullShaderpackSource();

    @Override
    public String getName() {
        return "OFF";
    }
}
