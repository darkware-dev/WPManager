/*******************************************************************************
 * Copyright (c) 2016. darkware.org and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.darkware.wpman.actions;

import org.darkware.wpman.WPComponent;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.agents.WPPeriodicAgent;
import org.darkware.wpman.util.TimeWindow;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The {@code WPActionService} is a service facade that provides helper methods to access and
 * monitor a {@link ScheduledExecutorService}.
 *
 * @author jeff
 * @since 2016-01-25
 */
public class WPActionService extends WPComponent
{
    /** A {@code Logger} available for package use to log various execution messages. **/
    protected static final Logger log = LoggerFactory.getLogger("Actions");

    private final ScheduledExecutorService execService;

    private final Set<WPPeriodicAgent> periodicAgents;
    private final Set<WPAction> scheduledActions;

    /**
     * Creates a new {@code WPActionService}, along with its own {@link ScheduledExecutorService}
     * with a fixed thread pool size.
     */
    public WPActionService()
    {
        super();

        //TODO: Make this configurable
        this.execService = Executors.newScheduledThreadPool(4);

        this.periodicAgents = new ConcurrentHashSet<>();
        this.scheduledActions = new ConcurrentHashSet<>();
    }

    /**
     * Schedule the repeated execution of a {@link WPPeriodicAgent} on the service. This creates a
     * repeated scheduling of the agent at a rate that is fixed to the {@code ScheduledExecutorService}.
     * It does not extend the schedule based on the runtime of any particular invocation of the agent.
     *
     * @param agent The agent to schedule.
     */
    public void schedule(final WPPeriodicAgent agent)
    {
        this.execService.scheduleAtFixedRate(agent, 0, agent.getPeriod(), TimeUnit.SECONDS);
        this.periodicAgents.add(agent);
    }

    /**
     * Schedule a {@code WPAction} for a single execution.
     *
     * @param action The action to execute.
     * @param <T> The return type of the action.
     * @return The {@link Future} for the action execution.
     */
    public <T> Future<T> scheduleAction(final WPAction<T> action)
    {
        Future<T> future = this.execService.submit(action);
        action.registerFuture(future);
        this.scheduledActions.add(action);

        return future;
    }

    /**
     * Schedule an action with a delay before execution. This works in a similar way to
     * {@link #scheduleAction(WPAction)}.
     *
     * @param action The action to execute.
     * @param delay The number of seconds to delay execution
     * @param <T> The return type of the action.
     * @return The {@link ScheduledFuture}
     */
    public <T> ScheduledFuture<T> scheduleAction(final WPAction<T> action, long delay)
    {
        ScheduledFuture<T> future = this.execService.schedule(action, delay, TimeUnit.SECONDS);
        action.registerFuture(future);
        this.scheduledActions.add(action);

        return future;
    }

    /**
     * Schedule an action with a delay before execution. This works in a similar way to
     * {@link #scheduleAction(WPAction)}.
     *
     * @param action The action to execute.
     * @param delay The number of seconds to delay execution
     * @param <T> The return type of the action.
     * @return The {@link ScheduledFuture}
     */
    public <T> ScheduledFuture<T> scheduleAction(final WPAction<T> action, final Duration delay)
    {
        return this.scheduleAction(action, delay.get(ChronoUnit.SECONDS));
    }

    /**
     * Schedule an action to occur within a given {@link TimeWindow}. This works in a similar way to
     * {@link #scheduleAction(WPAction)}, but with a delay so the execution falls within the window.
     *
     * @param action The action to execute.
     * @param window The {@code TimeWindow} the action should execute within.
     * @param <T> The return type of the action.
     * @return The {@link ScheduledFuture}
     * @see #scheduleAction(WPAction, Duration)
     */
    public <T> ScheduledFuture<T> scheduleAction(final WPAction<T> action, final TimeWindow window)
    {
        return this.scheduleAction(action, window.getRandomOffset());
    }

    /**
     * Shut down the service and terminate the associated {@code ExecutorService}. Any jobs that are
     * currently executing will have a chance to complete. If they take longer than the shutdown timeout
     * value, they will be forceably terminated via {@link Thread#interrupt()}.
     */
    public void shutdown()
    {
        try
        {
            WPActionService.log.info("Waiting for action service to complete any queued actions.");
            this.execService.shutdown();
            boolean allDone = this.execService.awaitTermination(30, TimeUnit.SECONDS);
            if (!allDone) WPActionService.log.info("Timeout exceeded. Forcing shutdown.");
        }
        catch (InterruptedException e)
        {
            WPActionService.log.warn("Shutdown wait terminated via thread interrupt. Forcing shutdown.");
        }

        this.execService.shutdownNow();
        WPActionService.log.info("Action service has shut down.");
    }

    /**
     * Fetch the {@code WPAction}s tracked by the service. This includes actions that are waiting to be
     * executed and actions that have recently executed.
     *
     * @return An unmodifiable {@code Set} of {@code WPAction} objects.
     */
    public Set<WPAction> getActions()
    {
        this.expireActions();

        return Collections.unmodifiableSet(this.scheduledActions);
    }

    /**
     * Clean the actions that are expired.
     */
    private void expireActions()
    {
        int before = this.scheduledActions.size();
        this.scheduledActions.removeIf(this::isExpired);
        int after = this.scheduledActions.size();

        WPManager.log.info("Expired " + (before - after) + " actions.");
    }

    /**
     * Checks to see if a given action should be expired from the list of tracked actions. An action is
     * expired if it has reached a completed state &mdash;regardless of the result&mdash; beyond the
     * grace period where actions are kept around for reporting.
     * <p>
     * In checking for completion, this method will attempt to resolve a state where the execution
     * {@code Future} reports a cancelled execution but the action is not yet aware of that.
     *
     * @param action The {@code WPAction} to be checked.
     * @return {@code true} if the action has passed the expiration for action tracking, {@code false} if
     * it should continue to be tracked.
     */
    private boolean isExpired(final WPAction action)
    {
        if (action.getCompletionTime() == null && action.getFuture().isCancelled())
        {
            action.cancel();
        }

        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);

        if (action.getCompletionTime() == null) return false;
        if (action.getCompletionTime().isBefore(expireTime)) return true;

        return false;
    }
}
