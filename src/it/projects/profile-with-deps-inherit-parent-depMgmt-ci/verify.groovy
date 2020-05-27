File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
assert 0 ==  originalProject.dependencies.size()
assert 'java9' == originalProject.profiles.profile.id.text()
assert 1 == originalProject.profiles.profile.dependencies.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 0 ==  flattendProject.dependencies.size()
assert null != flattendProject.parent
assert 1 ==  flattendProject.profiles.profile.dependencies.size()
assert '' ==  flattendProject.profiles.profile.dependencies.dependency.version.text()
