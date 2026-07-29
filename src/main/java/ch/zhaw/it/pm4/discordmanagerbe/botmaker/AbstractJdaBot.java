package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base class for all JDA Discord bot implementations.
 * Provides common functionality and JDA instance management for Discord bots.
 */
public abstract class AbstractJdaBot {

    /**
     * The JDA instance used for Discord API communication.
     */
    protected final JDA jdaBean;

    /**
     * Constructs an AbstractJdaBot with the specified JDA instance.
     *
     * @param jdaBean the JDA instance for Discord API operations
     */
    @Autowired
    public AbstractJdaBot(JDA jdaBean) {
        this.jdaBean = jdaBean;
    }
}
