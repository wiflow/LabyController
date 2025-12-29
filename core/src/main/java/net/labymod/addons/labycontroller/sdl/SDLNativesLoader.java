package net.labymod.addons.labycontroller.sdl;

import dev.isxander.sdl3java.jna.SdlNativeLibraryLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class SDLNativesLoader {

    private static boolean hasAttemptedLoad = false;
    private static LoadedSDLNatives loadedNatives = null;
    private static Path extractedNativePath = null;

    private SDLNativesLoader() {}

    public static Optional<LoadedSDLNatives> get() {
        return Optional.ofNullable(loadedNatives);
    }

    public static boolean hasAttemptedLoad() {
        return hasAttemptedLoad;
    }

    public static boolean isLoaded() {
        return loadedNatives != null;
    }

    public static Optional<LoadedSDLNatives> getOrLoad() {
        tryLoad();
        return get();
    }

        private static String getNativeResourcePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String jnaPrefix;
        String fileName;
        String extension;

        if (os.contains("win")) {
            jnaPrefix = "win32-x86-64";
            fileName = "SDL3";
            extension = "dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                jnaPrefix = "darwin-aarch64";
            } else {
                jnaPrefix = "darwin-x86-64";
            }
            fileName = "libSDL3";
            extension = "dylib";
        } else {
            // Linux
            if (arch.contains("aarch64") || arch.contains("arm")) {
                jnaPrefix = "linux-aarch64";
            } else {
                jnaPrefix = "linux-x86-64";
            }
            fileName = "libSDL3";
            extension = "so";
        }

        return "/" + jnaPrefix + "/" + fileName + "." + extension;
    }

        private static Path extractNative() throws IOException {
        String resourcePath = getNativeResourcePath();
        System.out.println("[Controlify] Looking for native at resource path: " + resourcePath);

        try (InputStream is = SDLNativesLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Native library not found in resources: " + resourcePath);
            }

            // Create temp directory for natives
            Path tempDir = Files.createTempDirectory("controlify-sdl3");
            tempDir.toFile().deleteOnExit();

            // Extract the native library
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            Path nativePath = tempDir.resolve(fileName);

            System.out.println("[Controlify] Extracting native to: " + nativePath);
            Files.copy(is, nativePath, StandardCopyOption.REPLACE_EXISTING);
            nativePath.toFile().deleteOnExit();

            return nativePath;
        }
    }

        public static boolean tryLoad() {
        if (hasAttemptedLoad) {
            return isLoaded();
        }

        hasAttemptedLoad = true;

        try {
            // Extract native library from JAR resources
            extractedNativePath = extractNative();

            System.out.println("[Controlify] Loading SDL3 from extracted path: " + extractedNativePath);

            // Load the native library from the extracted path
            SdlNativeLibraryLoader.loadLibSDL3FromFilePathNow(extractedNativePath.toAbsolutePath().toString());

            loadedNatives = new LoadedSDLNatives();
            loadedNatives.startSDL3();

            System.out.println("[Controlify] Successfully loaded SDL3 natives");

            return true;
        } catch (IOException e) {
            System.err.println("[Controlify] Failed to extract SDL3 native: " + e.getMessage());
        } catch (UnsatisfiedLinkError e) {
            System.err.println("[Controlify] Failed to link SDL3 library: " + e.getMessage());
        } catch (Throwable e) {
            System.err.println("[Controlify] Failed to load SDL natives: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
