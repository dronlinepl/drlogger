#!/usr/bin/env groovy
import groovy.transform.Field

@Field
def nodesOfBuild = []
@Field
def labelsOfNodes = ['MacOs']//, 'debian']
@Field
def shutNode = false
@Field
def allNodesMatrix
@Field
def buildSpace
@Field
def svnRevision = -1
@Field
def branchString = ""


class DrNode {
    String name
    String mac
    String host
    Integer benchmark

    DrNode(String name, String host, String mac, Integer benchmark) {
        this.name = name
        this.mac = mac
        this.host = host
        this.benchmark = benchmark ?: 100
    }

    @Override
    @NonCPS
    public String toString() {
        return "DrNode{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", host='" + host + '\'' +
                ", benchmark=" + benchmark +
                '}';
    }
}

@NonCPS
def sortDrNodes(list) {
    list.sort { -it.benchmark }
}

@NonCPS
// has to be NonCPS or the build breaks on the call to .each
def echo_all(list) {
    list.each { item ->

        current = item.getNodeProperties()?.first()?.getEnvVars()?.get('NODE_BENCHMARK') ?: 500
        println "Node listed: ${item} [$current] ${item.toComputer().online} ${item.toComputer().getOfflineCause()}"

    }
}

@NonCPS
def getNodes(label) {
    comps =  jenkins.model.Jenkins.get().computers

//    for( x in comps){
//        println "LABEL: " + x.node.labelString
//    }

    comps.findAll { it.node.labelString.contains(label) && !it.node.toComputer().isTemporarilyOffline() }
            .sort { -((it.node.getNodeProperties()?.first()?.getEnvVars()?.get('NODE_BENCHMARK') ?: "100") as Integer) }
            .collect { it.node }
}


def echoX2(list) {
    for (int i = 0; i < list.size(); i++) {
        item = list[i].toString()
        //sh "echo $item"
        println item
    }
}

def tryConnect(listOfList) {
    def connected = 0
    for (int ll = 0; ll < listOfList.size(); ll++) {
        list = listOfList[ll]
        for (int i = 0; i < list.size(); i++) {

            currentNode = list[i]


            def makeNode = currentNode.name

            if (Jenkins.instance.getNode(makeNode).toComputer().isOnline()) {
                println "$nodesOfBuild"
                println "ONLINE ${makeNode}"
                nodesOfBuild.add (makeNode)
                println "$nodesOfBuild"
                connected ++
                break
                //return true
            } else {
                //println "to w ogole z dupy jest do sprawdzenia"
                println "OFFLINE $makeNode"
                continue
                if (currentNode.mac != null) {

                    currentNode.toString()

                    sh "echo ${currentNode.toString()}"
                    sh "wol --verbose --host=${currentNode.host} --port=9 ${currentNode.mac}"

                    try {
                        timeout(3) {
                            waitUntil {
                                try {
                                    println "checking is ${makeNode} online"
                                    return Jenkins.instance.getNode(makeNode).toComputer().isOnline()
                                } catch (exception) {
                                    println "still offline ${makeNode} exception: $exception"
                                    return false
                                }
                            }
                        }
                        shutNode = true
                        println "ONLINE ${makeNode} shutNode = $shutNode"
                        return true
                    } catch (ex) {
                        println "catch ${makeNode} exception: $ex"
                    }
                    println "OFFLINE ${makeNode}"
                    //sh "echo BAAAAAAAAAAAAAAAAAAAAAAAADwol --verbose --host=${currentNode.host}  ${currentNode.mac}"
                }else{
                    println "NO MAC ${makeNode}"
                }
            }

        }
    }

    if (listOfList.size() == connected && connected > 0 ){
        //println "SELECTED NODES: $nodesOfBuild"
        return true
    }else{

        error('NO NODE AVALABLE')
        return false
    }
}

/* def trySuspendNode(){
    if (shutNode) {
        println "SUSPEND $makeNode"
        sh 'echo sleep 5s > ./jenkins_suspend.sh'
        sh 'echo sudo pm-suspend > ./jenkins_suspend.sh'
        sh 'chmod a+x ./jenkins_suspend.sh'
        sh './jenkins_suspend.sh&'
    }else{
        println "NO SUSPEND"
    }
} */

@NonCPS
def getDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    def ret =  item.getDescription()
    print "DESC" + ret
    return ""+ret
}

abcs = ['a', 'b', 'c']

pipeline {
    options {

        buildDiscarder(
                logRotator(
                        // number of build logs to keep
                        numToKeepStr: '30',
                        // history to keep in days
                        daysToKeepStr: '90',
                        // artifacts are kept for days
                        artifactDaysToKeepStr: '90',
                        // number of builds have their artifacts kept
                        artifactNumToKeepStr: '30'
                )
        )
        skipDefaultCheckout(true)
    }
    agent none
    environment {
        DESCRIPTION = getDescription()
    }

/*     tools {  // for gradle 8.0
        jdk 'JDK17-Bespin'
    } */

    stages {
        stage('Master') {
            agent { label 'master' }
            steps {
                println 'HELLO WORLD'
                script {
                    //def nodes = getNodes(labelsOfNodes[0])
                    def nodesMatrix  = labelsOfNodes.collect{ getNodes(it) }

                    nodesMatrix.each{
                        println "$it"
                        println "----------------------------"
                        echo_all(it)
                    }

                    println "----------$nodesMatrix------------------"

                    allNodesMatrix = nodesMatrix.collect { nodes ->
                        nodes.collect {
                            new DrNode(it.selfLabel.name,
                                    it.toComputer().launcher.host,
                                    it.getNodeProperties()?.first()?.getEnvVars()?.get('MAC_ADDRESS'),
                                    it.getNodeProperties()?.first()?.getEnvVars()?.get('NODE_BENCHMARK') as Integer
                            )
                        }
                    }
                    println "----------$allNodesMatrix------------------"
//                     println "====NOT SORTED===="
//                     allNodesMatrix.each{
//                         echoX2(it)
//                     }

                    ///sleep( 2 )
                    allNodesMatrix.each{
                        sortDrNodes(it)
                    }
                    println "====SORTED===="
                    allNodesMatrix.each{
                        echoX2(it)
                    }

                }
                sh 'hostname'
                sh 'ifconfig'
                tryConnect(allNodesMatrix)



            }
        }

         stage('Checkout') {
            //options { skipDefaultCheckout(false) }
            agent { label nodesOfBuild[0] }
            steps {

                script {
                    def makeNode = ""

                    def wk = "$WORKSPACE"
                    def wksplit =wk.split("/") as String[]
                    def wkex = wksplit.last()


                    //TODO: nie tak
                    buildSpace="$TMP/$wkex"

                    //println 'Pulling... ' + env.GIT_BRANCH

                    if (branchString == ""){

                        def branchNameCfg = scm.branches[0].name
                        println 'BranchName ... ' + branchNameCfg
                        def  branchSplit  = branchNameCfg.split("/") as String[]
                        branchString = branchSplit.last()
                        println 'branchString ... ' + branchString
                    }

                    def scmVars = checkout scm
                    //svnRevision = scmVars.SVN_REVISION
                    //error('Stopping early…')

                    if (svnRevision == -1){
                        revRaw = sh(returnStdout: true, script: 'git log --oneline | wc -l')
                        svnRevision = 800 +revRaw.toInteger()
                    }

                    makeNode = NODE_NAME

                    echo "makeNode: $makeNode, NODE_NAME: $NODE_NAME, buildSpace=$buildSpace, svnRevision=$svnRevision"

                }
                sh 'chmod a+x ./gradlew'
                // DEPRECATED - USE ANDROID_HOME
                sh 'set'
                //sh 'echo sdk.dir=/Users/srv_jenkins/android-sdk>./local.properties'


            }
        }

         stage('clean build archive') {
            tools {  // for gradle 8.0
                 jdk JDK17
             }
            agent { label nodesOfBuild[0] }
            steps {
                echo "NODE_NAME: $NODE_NAME"
                //error('Stopping early…')
                withCredentials([usernamePassword(credentialsId: 'mavenCentralCredentials', usernameVariable: 'MAVEN_USERNAME', passwordVariable: 'MAVEN_PASSWORD'),
                                 string(credentialsId: 'OpenPGP_keyID', variable: 'SIGNING_KEY_ID'),
                                 string(credentialsId: 'OpenPGP_password', variable: 'SIGNING_PASSWORD'),
                                 string(credentialsId: 'OpenPGP_secretKeyRingText', variable: 'SECRET_KEY_RING')]) {
                              withEnv([
                                    "ORG_GRADLE_PROJECT_signingInMemoryKey=$SECRET_KEY_RING",
                                    "ORG_GRADLE_PROJECT_signingInMemoryKeyId=$SIGNING_KEY_ID",
                                    "ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$SIGNING_PASSWORD",
                                    "ORG_GRADLE_PROJECT_mavenCentralUsername=$MAVEN_USERNAME",
                                    "ORG_GRADLE_PROJECT_mavenCentralPassword=$MAVEN_PASSWORD"
                                ]) {
                                    echo sh(returnStdout: true, script: 'env')
                                    sh './gradlew publishAndReleaseToMavenCentral'
                                }
                   }

            }



        }

        stage('tag') {
            agent { label nodesOfBuild[0] }
            steps {
                script {
                    def VTAG = sh(script: './gradlew -q printVersion --no-configuration-cache | tail -1', returnStdout: true).trim()

                    echo "NODE_NAME: $NODE_NAME, VTAG:$VTAG"

                    def versionFile = "version-${VTAG}.txt"
                    writeFile file: versionFile, text: VTAG

                    sshagent(credentials: ['43105f25-c22b-42e9-ba2a-b4a6895e512c']) {
                        sh("git tag -a ${VTAG} -m 'Jenkins'")
                        sh('git push origin --tags')
                    }
                }

            }
        }

        stage('archiveArtifacts') {
            agent { label nodesOfBuild[0] }
            steps {
                echo "NODE_NAME: $NODE_NAME"
                archiveArtifacts 'version*.txt'
            }
        }

/*         stage('Sonarqube') {
            agent { label makeNode }
            when {
                expression {
                    return !DESCRIPTION.contains('NO_SONAR')
                }
            }
            environment {
                scannerHome = tool 'SonarQube'
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh "${scannerHome}/bin/sonar-scanner -X -Dsonar.projectVersion=${svnRevision} -Dfile.encoding=UTF8 -Dsonar.host.url=${env.SONAR_HOST_URL} -Djavax.net.ssl.keyStore=~/.keyStore"
                }
            }
        }
*/

    }
/*     post {
        always {
            node(makeNode) {
                trySuspendNode()
            }
        }
    } */
}