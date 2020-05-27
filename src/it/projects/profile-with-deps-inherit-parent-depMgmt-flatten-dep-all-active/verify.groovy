File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
assert 0 ==  originalProject.dependencies.size()
assert 'test' == originalProject.profiles.profile.id.text()
assert 1 == originalProject.profiles.profile.dependencies.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 2 ==  flattendProject.dependencies.dependency.size()
assert 'core' ==  flattendProject.dependencies.dependency[0].artifactId.text()
assert 1 == flattendProject.dependencies.dependency[0].exclusions.exclusion.size()
assert 'dep' == flattendProject.dependencies.dependency[1].artifactId.text()
assert 1 ==  flattendProject.profiles.profile.dependencies.dependency.size()
assert 'core' ==  flattendProject.profiles.profile.dependencies.dependency.artifactId.text()
assert '3.2.1' ==  flattendProject.profiles.profile.dependencies.dependency.version.text()
