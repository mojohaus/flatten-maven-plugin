File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 'http://${repoHost}/${basedir}' == flattendProject.profiles.profile[0].repositories.repository[0].url.text()
