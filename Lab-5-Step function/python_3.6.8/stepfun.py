import boto3
import subprocess
ROLE = subprocess.getoutput('aws iam list-roles | grep role/lab5-states-role | tr -d "," | cut -f2- -d: | xargs')
client = boto3.client('stepfunctions')

response = client.create_state_machine(
    name='Fancy-StateMachine',
    definition=open("/home/ec2-user/environment/resources/definition.json").read(),
    roleArn=ROLE
)
print("DONE")