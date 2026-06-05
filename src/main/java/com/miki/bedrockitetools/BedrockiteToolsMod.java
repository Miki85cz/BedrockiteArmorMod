package com.miki.bedrockitetools;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.TickTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

import com.miki.bedrockitetools.init.BedrockiteToolsModItems;


@Mod("bedrockitetools")
public class BedrockiteToolsMod {
    public static final Logger LOGGER = LogManager.getLogger(BedrockiteToolsMod.class);
    public static final String MODID = "bedrockitetools";

    public BedrockiteToolsMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerNetworking);
        BedrockiteToolsModItems.REGISTRY.register(modEventBus);

        // FIX: The creative tab listener is now correctly placed inside the constructor
        modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey().location().toString().equals("bedrockite:bedrockite")) {
                event.accept(new ItemStack(BedrockiteToolsModItems.BEDROCKITE_SWORD.get()));
                event.accept(new ItemStack(BedrockiteToolsModItems.BEDROCKITE_AXE.get()));
                event.accept(new ItemStack(BedrockiteToolsModItems.BEDROCKITE_SHOVEL.get()));
                event.accept(new ItemStack(BedrockiteToolsModItems.BEDROCKITE_HOE.get()));
            }
        });
    }

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
    }

    public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        if (networkingRegistered)
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
        networkingRegistered = true;
    }

    private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
    private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

    public static void queueServerWork(int delay, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        int currentTick = event.getServer().getTickCount();
        IntObjectPair<Runnable> work;
        while ((work = workToBeScheduled.poll()) != null) {
            workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
        }
        while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
            workQueue.poll().run();
        }
    }
} 