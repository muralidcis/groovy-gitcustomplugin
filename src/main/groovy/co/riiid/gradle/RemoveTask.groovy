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

    def path = "/repos/${project.github.owner}/${project.github.repo}/releases/"


    //DELETE /repos/:owner/:repo/releases/:id

    http.request(Method.GET) {

      uri.path += "${path}tags/${project.github.getTagName()}"

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
	println "id:: "+json.id

	http.request(Method.DELETE) {
	  uri.path += "$path$json.id"

	  requestContentType = ContentType.JSON

	  headers.'User-Agent' = HEADER_USER_AGENT
	  headers.'Authorization' = "token ${project.github.token}"
	  headers.'Accept' = accept

	  response.success = { respInfo, jsonInfo ->
	    logger.debug "< $respInfo.statusLine"
	    println "Deleted! "+json.id
	  }

	  response.failure ={ respInfo,jsonInfo ->
	    logger.error "Error while deleting"+json.id
	    logger.debug ' Error on deleteing: \n' + respInfo.headers.collect { "< $it" }.join('\n')
	  }
	}
      }

      response.failure = { resp, json ->
	logger.error "Error in $postLogMessage"
	logger.debug 'Response headers: \n' + resp.headers.collect { "< $it" }.join('\n')
	def errorMessage = json?json.message:resp.statusLine
	def ref = json?"See $json.documentation_url":''
	def errorDetails = json && json.errors? "Details: " + json.errors.collect { it }.join('\n'):''
	throw new GradleScriptException("$errorMessage. $ref. $errorDetails", null)
      }
    }
  }
}
