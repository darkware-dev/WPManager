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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author jeff
 * @since 2016-02-10
 */
public interface WPAction<T> extends Callable<T>
{
    /**
     * Checks to see if this action has requested a timeout value be applied to its
     * execution.
     *
     * @return {@code true} if the action has a timeout, {@code false} if not.
     */
    @JsonIgnore
    boolean hasTimeout();

    /**
     * Fetches the number of seconds this action is allowed to execute for, or zero if no
     * timeout is enabled.
     *
     * @return The timeout, in seconds, or zero if no timeout is requested.
     */
    @JsonProperty
    int getTimeout();

    /**
     * Fetch a description of the goal of this action.
     *
     * @return The description as a {@code String}.
     */
    @JsonProperty
    String getDescription();

    /**
     * Fetch the state of this action. This declares where the action is in the normal execution
     * lifecycle.
     *
     * @return The current {@link WPActionState}.
     */
    @JsonProperty
    WPActionState getState();

    /**
     * Fetch the time this action was created.
     *
     * @return A {@code DateTime} representing the moment this action was created.
     */
    @JsonProperty
    LocalDateTime getCreationTime();

    /**
     * Fetch the time this action began execution.
     *
     * @return A {@code DateTime} representing the moment this action began execution, or {@code null}
     * if the action has not started execution.
     */
    @JsonProperty
    LocalDateTime getStartTime();

    /**
     * Fetch the time this action completed execution, regardless of the outcome.
     *
     * @return A {@code DateTime} representing the moment this action finished execution, or {@code null}
     * if the action has not yet finished (or began).
     */
    @JsonProperty
    LocalDateTime getCompletionTime();

    /**
     * Register an execution {@link Future} with this action. This can be used for various
     * job-control functions.
     * <p>
     * It is assumed that any non-repeating action will have its {@code Future} associated with it
     * via this method.
     *
     * @param future The {@code Future} for this action.
     */
    void registerFuture(Future<T> future);

    /**
     * Fetch the {@code Future} registered with this action.
     *
     * @return A {@code Future} for this action, or {@code null} if the action is not being executed.
     */
    @JsonIgnore
    Future<T> getFuture();

    /**
     * Cancels this action, attempting to prevent future execution. This relies upon a registered
     * {@link Future} via the {@link #registerFuture(Future)} method.
     */
    void cancel();
}
