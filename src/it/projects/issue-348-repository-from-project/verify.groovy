
import groovy.xml.XmlSlurper

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 1 ==  flattendProject.repositories.size()

assert 1 ==  flattendProject.dependencies.size()
assert 'spring-core' ==  flattendProject.dependencies.dependency.artifactId.text()
assert '6.0.0-RC4' ==  flattendProject.dependencies.dependency.version.text()
