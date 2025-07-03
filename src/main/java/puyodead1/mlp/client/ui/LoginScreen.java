package puyodead1.mlp.client.ui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.utils.MLPSystem;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.ForkJoinPool;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LoginScreen extends Screen {
    private static final Identifier MLP_TITLE = Identifier.of("mlp:title.png");
    private ButtonWidget loginButton;
    private String errorMessage;
    private boolean isChecking = false;
    private TextFieldWidget tokenField;
    private final MLPService.LoginRequest request = new MLPService.LoginRequest();


    public LoginScreen() {
        super(Text.of("Login"));
    }

    @Override
    protected void init() {
        this.tokenField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 4 + 40 + 12, 200, 20, Text.translatable("addServer.enterIp"));
        this.tokenField.setMaxLength(128);
        if(!MLPSystem.get().accessToken.isEmpty()) this.tokenField.setText(MLPSystem.get().accessToken);
        this.tokenField.setChangedListener((text) -> this.onTokenFieldChanged());
        this.addSelectableChild(this.tokenField);

        this.loginButton = this.addDrawableChild(ButtonWidget.builder(Text.of("Login"), (button) -> this.doAuth()).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20).build());
        this.loginButton.active = false;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Quit"), (button) -> this.client.scheduleStop()).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());

        if (MLPSystem.get().accessToken != null && !MLPSystem.get().accessToken.isEmpty() && !isChecking) {
            this.validateAuth();
        }

        this.onTokenFieldChanged();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.tokenField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.tokenField.getText();
        this.init(client, width, height);
        this.tokenField.setText(string);
    }

    private void onTokenFieldChanged() {
        this.loginButton.active = this.tokenField.getText().length() > 10;
        this.request.auth.setLogin(this.tokenField.getText());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // render Logo
        int texWidth = 1021 / 2;
        int texHeight = 77 / 2;

        int j = ColorHelper.getWhite(1.0F);
        int i = (this.width / 2) - (texWidth / 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, MLP_TITLE, i, 30, 0.0f, 0.0f, texWidth, texHeight, texWidth, texHeight, j);

        int halfWidth = this.width / 2;

        context.drawTextWithShadow(this.textRenderer, "MCSDC Login", halfWidth - 100 + 1, this.height / 4 + 24 + 12, Colors.WHITE);
        this.tokenField.render(context, mouseX, mouseY, delta);

        if (this.errorMessage != null) {
            context.drawTextWithShadow(this.textRenderer, Text.of(this.errorMessage), halfWidth - 100 + 1, this.height / 4 + 70 + 12, Colors.LIGHT_RED);
        }

        this.loginButton.render(context, mouseX, mouseY, delta);
    }

    private void doAuth() {
        if (this.client == null) return;

        this.loginButton.setMessage(Text.of("Login"));

        ForkJoinPool.commonPool().submit(() -> {
            mc.execute(this::validateAuth);
        });
    }

    private void validateAuth() {
        if (this.client == null) return;

        isChecking = true;
        Text originalText = this.loginButton.getMessage();
        this.loginButton.active = false;
        this.loginButton.setMessage(Text.of("Validating Token..."));

        ForkJoinPool.commonPool().submit(() -> {
            checkAuth(originalText);
        });
    }

    private void checkAuth(Text originalText) {
        final String ERROR_TEXT = "Failed to login.";

        try {
            MLPService.LoginResponse res = MLPMod.getMLPService().login(request);

            mc.execute(() -> {
                try {
                    if (res == null) {
                        setErrorState(ERROR_TEXT);
                    } else {
                        MLPSystem.get().setUsername(res.data.name);
                        MLPSystem.get().setAccessToken(res.data.token);
                        MLPSystem.get().setPermLevel(res.data.perms);
                        MLPSystem.get().save();
                        this.client.setScreen(new TitleScreen(true));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setErrorState(ERROR_TEXT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            String error = e.getMessage();
            if (error.equals("unauthorized")) {
                setErrorState("Invalid Token");
            } else {
                setErrorState(error);
            }
        } finally {
            isChecking = false;
            this.loginButton.setMessage(originalText);
            this.loginButton.active = true;
        }
    }

    private void setErrorState(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
