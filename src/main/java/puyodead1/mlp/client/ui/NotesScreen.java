package puyodead1.mlp.client.ui;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.client.ui.serverlist.MLPMultiplayerServerListWidget;

import java.util.concurrent.ForkJoinPool;

public class NotesScreen extends WindowScreen {
    private final MLPService.Server server;
    private final MLPService mlpService;
    private WTextBox textBox;
    private WLabel label;


    public NotesScreen(MLPService.Server server, MLPService mlpService) {
        super(GuiThemes.get(), "Notes: " + server.displayServerAddress());
        this.server = server;
        this.mlpService = mlpService;
    }

    @Override
    public void initWidgets() {
        String notes = this.server.notes;

        if (notes == null) notes = "--- empty ---";

        label = theme.label(notes);
        add(label).expandX();

        textBox = theme.textBox(notes);
        textBox.visible = false;
        add(textBox).expandX();

        WButton button = add(theme.button("Edit")).expandX().widget();
        button.action = () -> {
            if(button.getText().equals("Edit")) {
                // switch to editing mode
                label.visible = false;
                textBox.visible = true;
                textBox.setFocused(true);
                button.set("Save");
            } else {
                // save the changes and switch to viewing mode
                if(!textBox.get().equals(label.get())) {
                    // changes, save them
                    label.set(textBox.get());
                    this.server.notes = textBox.get();
                    label.visible = true;
                    textBox.visible = false;
                    button.set("Saving...");

                    MLPService.UpdateServerRequest req = new MLPService.UpdateServerRequest();
                    req.update.setAddress(this.server.address);
                    req.update.setNotes(textBox.get());

                    ForkJoinPool.commonPool().submit(() -> {
                        this.mlpService.update(req, () -> {
                            button.set("Edit");
                        });
                    });
                }
            }
        };
    }
}
