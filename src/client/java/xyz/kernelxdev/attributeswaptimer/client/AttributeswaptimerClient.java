package xyz.kernelxdev.attributeswaptimer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeswaptimerClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("attributeswaptimer");

    private static final int PERFECT_SWAP_TICKS = 1;
    private static final int MAX_TRACKING_TICKS = 20;

    private ItemStack lastMainHandItem = ItemStack.EMPTY;
    private ItemStack previousTickItem = ItemStack.EMPTY;
    private long lastAttackTime = 0;
    private boolean trackingSwap = false;
    private int ticksSinceAttack = 0;
    private float lastAttackCooldown = 1.0f;
    private boolean lastHandSwinging = false;

    private ItemStack itemOnCooldownReset = ItemStack.EMPTY;
    private boolean trackingAttack = false;
    private int ticksSinceCooldownReset = 0;

    private int displayTicks = 0;
    private long displayMs = 0;
    private double displaySeconds = 0;
    private String timingResult = "";
    private long displayUntil = 0;
    private static final long DISPLAY_DURATION = 500;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Attribute Swap Timer initializing...");

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("attributeswaptimer", "swap_timer"),
                this::renderHud
        );

        LOGGER.info("Attribute Swap Timer initialized successfully!");
    }

    private void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        ItemStack currentMainHand = player.getItemInHand(InteractionHand.MAIN_HAND);

        float currentAttackCooldown = player.getAttackStrengthScale(0.0f);
        boolean currentHandSwinging = player.swinging;

        boolean attackCooldownReset = lastAttackCooldown > 0.9f && currentAttackCooldown < 0.9f;
        boolean handSwingStarted = !lastHandSwinging && currentHandSwinging;

        if (attackCooldownReset) {
            itemOnCooldownReset = previousTickItem.copy();
            trackingAttack = true;
            ticksSinceCooldownReset = 0;
            LOGGER.debug("Cooldown reset detected. Item: {}", itemOnCooldownReset.getHoverName().getString());
        }

        if (trackingAttack) {
            ticksSinceCooldownReset++;

            if (handSwingStarted) {
                lastAttackTime = System.currentTimeMillis();
                lastMainHandItem = itemOnCooldownReset.copy();
                trackingSwap = true;
                ticksSinceAttack = 0;
                trackingAttack = false;

                LOGGER.info("Attack confirmed! Item: {}", lastMainHandItem.getHoverName().getString());
            }

            if (ticksSinceCooldownReset > 5) {
                trackingAttack = false;
                LOGGER.debug("Attack false alarm. Cooldown reset without swing.");
            }
        }

        lastAttackCooldown = currentAttackCooldown;
        lastHandSwinging = currentHandSwinging;
        previousTickItem = currentMainHand.copy();
        if (trackingSwap) {
            ticksSinceAttack++;

            if (!ItemStack.isSameItem(lastMainHandItem, currentMainHand)) {
                long swapTime = System.currentTimeMillis();
                long timeDiff = swapTime - lastAttackTime;

                displayTicks = ticksSinceAttack;
                displayMs = timeDiff;
                displaySeconds = timeDiff / 1000.0;

                int ticksOff = displayTicks - PERFECT_SWAP_TICKS;

                if (ticksOff == 0) {
                    timingResult = "PERFECT!";
                } else if (Math.abs(ticksOff) == 1) {
                    timingResult = "Good (+1 tick)";
                } else if (ticksOff == 2) {
                    timingResult = "Late (+2 ticks)";
                } else {
                    timingResult = "Too Late (+" + ticksOff + " ticks)";
                }

                displayUntil = System.currentTimeMillis() + DISPLAY_DURATION;
                trackingSwap = false;

                LOGGER.info("Swap detected! From: {} To: {} | Ticks: {} | Ms: {} | Result: {}",
                        lastMainHandItem.getHoverName().getString(),
                        currentMainHand.getHoverName().getString(),
                        displayTicks,
                        displayMs,
                        timingResult);
            }

            if (ticksSinceAttack > MAX_TRACKING_TICKS) {
                trackingSwap = false;
                LOGGER.debug("Tracking timeout - no swap detected");
            }
        }
    }

    private void renderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.options.hideGui) return;

        if (System.currentTimeMillis() > displayUntil) return;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        int resultLineY = (screenHeight / 2) - 100;

        int startY = resultLineY - 24;

        String ticksText = "Ticks: " + displayTicks;
        String msText = "Time: " + displayMs + "ms (" + String.format("%.3f", displaySeconds) + "s)";
        String resultText = timingResult;

        int resultColor;
        if (timingResult.startsWith("PERFECT")) {
            resultColor = 0xFF00FF00;
        } else if (timingResult.startsWith("Good")) {
            resultColor = 0xFFFFFF00;
        } else if (timingResult.startsWith("Late")) {
            resultColor = 0xFFFFA500;
        } else {
            resultColor = 0xFFFF0000;
        }

        int ticksWidth = client.font.width(ticksText);
        graphics.text(
                client.font,
                Component.literal(ticksText),
                (screenWidth - ticksWidth) / 2,
                startY,
                0xFFFFFFFF,
                true
        );

        int msWidth = client.font.width(msText);
        graphics.text(
                client.font,
                Component.literal(msText),
                (screenWidth - msWidth) / 2,
                startY + 12,
                0xFFFFFFFF,
                true
        );

        int resultWidth = client.font.width(resultText);
        graphics.text(
                client.font,
                Component.literal(resultText),
                (screenWidth - resultWidth) / 2,
                startY + 24,
                resultColor,
                true
        );
    }
}
