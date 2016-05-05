package co.riiid.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class GithubPlugin implements Plugin<Project> {
  
  // github{...} will collect all the configurations,
  // like username,token,tag etc wrapper them for githubRelease,and githubRemove tasks  
  private static final String NAME = 'github'

  // project Extentions for the tasks.
  void apply(Project project) {
    project.extensions.create(NAME, GithubExtension)
    project.task('githubRelease', type: ReleaseTask)
    project.task('githubRemove', type: RemoveTask)
  }
}
