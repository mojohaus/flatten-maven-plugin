File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
assert 0 == originalProject.dependencies.size()
assert 1 == originalProject.profiles.size()
assert 1 == originalProject.dependencyManagement.size()
assert 1 == originalProject.modules.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 0 ==  flattendProject.dependencies.size()
assert 1 ==  flattendProject.profiles.profile.dependencies.dependency.size()
assert 'dep' ==  flattendProject.profiles.profile.dependencies.dependency.artifactId.text()
assert '1.1' ==  flattendProject.profiles.profile.dependencies.dependency.version.text()

File flattendChildPom = new File( basedir, 'child/.flattened-pom.xml' )
assert flattendChildPom.exists()

def flattendChildProject = new XmlSlurper().parse( flattendChildPom )
assert 0 ==  flattendChildProject.dependencies.size()
assert 2 ==  flattendChildProject.profiles.profile.dependencies.dependency.size()
assert 'javax.annotation-api' ==  flattendChildProject.profiles.profile.dependencies.dependency[0].artifactId.text()
assert '1.3.2' ==  flattendChildProject.profiles.profile.dependencies.dependency[0].version.text()
assert 'dep' ==  flattendChildProject.profiles.profile.dependencies.dependency[1].artifactId.text()
assert '1.1' ==  flattendChildProject.profiles.profile.dependencies.dependency[1].version.text()