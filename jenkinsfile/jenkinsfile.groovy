pipeline {
  agent any
  options {
    timestamps()
    ansiColor('xterm')
  }
  parameters {
    choice(
      name: 'inventoryName',
      choices: "\nqa1\nqa2\nqa3\nqa-rel-loc\nqa-rel-ahoc\nbugfix\ncustomer-test",
      description: 'Choose which environment to run playbook in'
    )
    choice(
      name: 'ansiblePlaybook',
      choices: "playbooks/deployment-manager/deploy-epa-code.yml\nplaybooks/deployment-manager/deploy-epa-scripts.yml\nplaybooks/deployment-manager/download-package.yml\nplaybooks/deployment-manager/run-jacl.yml",
      description: 'Choose Ansible playbook to run'
    )
    choice(
      name: 'ansibleVerbosity',
      choices: '\n-v\n-vv\n-vvv\n-vvvv',
      description: 'Choose Ansible verbosity level'
    )
  }
  environment {
    appCode = 'epa'
  }
  stages {
    stage('Checkout') {
      steps {
        echo 'Checking out projects from Bitbucket....'
        dir('epa') {
          git branch: 'qa', url: 'git@github.com:vamsi8977/ansible.git'
        }
      }
    }
    stage('Ansible - Tomcat Setup') {
      steps {
        ansiColor('xterm') {
          echo 'Configuring instances with Ansible....'
          sh """
          cd epa/ansible;
          ansible-playbook -i inventories/${params.inventoryName} ${params.ansiblePlaybook} --extra-vars "epa_version=${Version}" ${params.ansibleVerbosity}
          """
        }
      }
    }
    } //end stages
    post {
/*      always {
        echo "Cleaning up after ourselves."
        cleanWs()
      } */
      success {
        echo "The build succeeded."
        mail to: 'vkbommasani86@gmail.com',
               subject: "Jenkins build changed/success: ${currentBuild.fullDisplayName}",
               body: "Jenkins build changed/success ${env.BUILD_URL}"
      }
      failure {
        echo "The build failed."
        mail to: 'vkbommasani@gmail.com',
               subject: "Build failed in Jenkins: ${currentBuild.fullDisplayName}",
               body: "Build failed in Jenkins ${env.BUILD_URL}"
      }
    }
  }
