package vazkii.botania.client.core.handler;

import com.google.common.base.Throwables;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import vazkii.botania.common.lib.LibObfuscation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class ClientMethodHandles {

    public static final MethodHandle
            renderPosX_getter, renderPosY_getter, renderPosZ_getter; // RenderManager

    static {
        try {
            Field f = ReflectionHelper.findField(RenderManager.class, LibObfuscation.RENDERPOSX);
            f.setAccessible(true);
            renderPosX_getter = MethodHandles.publicLookup().unreflectGetter(f);

            f = ReflectionHelper.findField(RenderManager.class, LibObfuscation.RENDERPOSY);
            f.setAccessible(true);
            renderPosY_getter = MethodHandles.publicLookup().unreflectGetter(f);

            f = ReflectionHelper.findField(RenderManager.class, LibObfuscation.RENDERPOSZ);
            f.setAccessible(true);
            renderPosZ_getter = MethodHandles.publicLookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            FMLLog.severe("[Botania]: Couldn't initialize client methodhandles! Things will be broken!");
            throw Throwables.propagate(e);
        }
    }

    private ClientMethodHandles() {}
}
