package co.riiid.gradle

import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleScriptException

class RemoveTask extends DefaultTask {

	// header
	final String HEADER_USER_AGENT = 'gradle-github-plugin'

	@TaskAction
	public remove() {
		def baseUrl = project.github.getBaseUrl()
		def accept = project.github.getAcceptHeader()

		def http = new HttpBuilder(baseUrl)

		//GET /repos/:owner/:repo/releases/tags/:tag
		
		def path = "/repos/" +
				"${project.github.owner}/" +
				"${project.github.repo}/releases/"

		http.request(Method.GET) {
			uri.path += path+"tags/"+project.github.getTagName()
			requestContentType = ContentType.JSON

			headers.'User-Agent' = HEADER_USER_AGENT
			headers.'Authorization' = "token ${project.github.token}"
			headers.'Accept' = accept

			def postLogMessage = "POST ${uri.path}\n" +
					" > User-Agent: ${headers['User-Agent']}\n" +
					" > Authorization: (not shown)\n" +
					" > Accept: ${headers.Accept}\n"
			logger.debug "$postLogMessage"

			response.success = { resp, json ->

				logger.debug "< $resp.statusLine"
				logger.debug 'Response headers: \n' + resp.headers.collect { "< $it" }.join('\n')
				println "name:: "+json.name
				println "id::"+json.id

				//DELETE /repos/:owner/:repo/releases/assets/:id
				http.request(Method.DELETE){ req ->
					uri.path = path+"assets/"+json.id
					requestContentType = ContentType.JSON
		
					headers.'User-Agent' = HEADER_USER_AGENT
					headers.'Authorization' = "token ${project.github.token}"
					headers.'Accept' = accept
					
					// Item exists, change POST to PUT
					response.success = { respInfo, jsonInfo ->
						println "SUCCESS: Successfully deleted item '${json.id}'"
					}
					response.failure = { respInfo,jsonInfo ->
						logger.debug 'Response headers: \n' + respInfo.headers.collect { "< $it" }.join('\n')
						throw new Exception("FAILURE: Could not delete item '${json.id}'. Error response: \n${resp.statusLine}")
					}
				}

			}

			response.failure = { resp, json ->
				logger.error "Error in $postLogMessage"
				logger.debug 'Response headers: \n' + resp.headers.collect { "< $it" }.join('\n')
			}
		}
	}

}

