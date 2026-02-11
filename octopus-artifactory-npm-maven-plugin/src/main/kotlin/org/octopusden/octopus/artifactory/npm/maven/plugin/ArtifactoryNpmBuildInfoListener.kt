package org.octopusden.octopus.artifactory.npm.maven.plugin

import org.apache.maven.execution.AbstractExecutionListener
import org.apache.maven.execution.ExecutionEvent
import org.apache.maven.execution.ExecutionListener

class ArtifactoryNpmBuildInfoListener(
    private val wrappedListener: ExecutionListener,
    private val onSessionEnded: (ExecutionEvent?) -> Unit
): AbstractExecutionListener() {
    override fun sessionEnded(event: ExecutionEvent?) {
        wrappedListener.sessionEnded(event)
        onSessionEnded(event)
    }
    override fun projectDiscoveryStarted(event: ExecutionEvent?) = wrappedListener.projectDiscoveryStarted(event)
    override fun sessionStarted(event: ExecutionEvent?) = wrappedListener.sessionStarted(event)
    override fun projectSkipped(event: ExecutionEvent?) = wrappedListener.projectSkipped(event)
    override fun projectStarted(event: ExecutionEvent?) = wrappedListener.projectStarted(event)
    override fun projectSucceeded(event: ExecutionEvent?) = wrappedListener.projectSucceeded(event)
    override fun projectFailed(event: ExecutionEvent?) = wrappedListener.projectFailed(event)
    override fun mojoSkipped(event: ExecutionEvent?) = wrappedListener.mojoSkipped(event)
    override fun mojoStarted(event: ExecutionEvent?) = wrappedListener.mojoStarted(event)
    override fun mojoSucceeded(event: ExecutionEvent?) = wrappedListener.mojoSucceeded(event)
    override fun mojoFailed(event: ExecutionEvent?) = wrappedListener.mojoFailed(event)
    override fun forkStarted(event: ExecutionEvent?) = wrappedListener.forkStarted(event)
    override fun forkSucceeded(event: ExecutionEvent?) = wrappedListener.forkSucceeded(event)
    override fun forkFailed(event: ExecutionEvent?) = wrappedListener.forkFailed(event)
    override fun forkedProjectStarted(event: ExecutionEvent?) = wrappedListener.forkedProjectStarted(event)
    override fun forkedProjectSucceeded(event: ExecutionEvent?) = wrappedListener.forkedProjectSucceeded(event)
    override fun forkedProjectFailed(event: ExecutionEvent?) = wrappedListener.forkedProjectFailed(event)
}