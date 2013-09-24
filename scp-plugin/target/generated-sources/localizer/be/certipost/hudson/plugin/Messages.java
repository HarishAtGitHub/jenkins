// CHECKSTYLE:OFF

package be.certipost.hudson.plugin;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

@SuppressWarnings({
    "",
    "PMD"
})
public class Messages {

    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);

    /**
     * Can't connect to server
     * 
     */
    public static String SCPRepositoryPublisher_NotConnect() {
        return holder.format("SCPRepositoryPublisher.NotConnect");
    }

    /**
     * Can't connect to server
     * 
     */
    public static Localizable _SCPRepositoryPublisher_NotConnect() {
        return new Localizable(holder, "SCPRepositoryPublisher.NotConnect");
    }

    /**
     * keyfile does not exist
     * 
     */
    public static String SCPRepositoryPublisher_KeyFileNotExist() {
        return holder.format("SCPRepositoryPublisher.KeyFileNotExist");
    }

    /**
     * keyfile does not exist
     * 
     */
    public static Localizable _SCPRepositoryPublisher_KeyFileNotExist() {
        return new Localizable(holder, "SCPRepositoryPublisher.KeyFileNotExist");
    }

    /**
     * Publish artifacts to SCP Repository
     * 
     */
    public static String SCPRepositoryPublisher_DisplayName() {
        return holder.format("SCPRepositoryPublisher.DisplayName");
    }

    /**
     * Publish artifacts to SCP Repository
     * 
     */
    public static Localizable _SCPRepositoryPublisher_DisplayName() {
        return new Localizable(holder, "SCPRepositoryPublisher.DisplayName");
    }

}
