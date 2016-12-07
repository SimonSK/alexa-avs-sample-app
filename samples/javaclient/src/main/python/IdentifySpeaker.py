import sys
from CognitiveSpeakerRecognition.Identification import IdentificationServiceHttpClientHelper


def print_all_profiles(client):
    """Print all the profiles for the given client.

    Arguments:
    client -- IdentificationServiceHttpClientHelper object
    """
    profiles = client.get_all_profiles()
    profile_ids = []

    for profile in profiles:
        profile_ids.append(profile.get_profile_id())
    return profile_ids


def identify_file(client, file_path, profile_ids, force_short_audio):
    """Identify an audio file on the server.

    Arguments:
    file_path -- the audio file path for identification
    profile_ids -- an array of test profile IDs strings
    force_short_audio -- waive the recommended minimum audio limit needed for enrollment
    """
    identification_response = client.identify_file(file_path, profile_ids, force_short_audio.lower() == "true")

    identified_profile_id = identification_response.get_identified_profile_id()
    confidence = identification_response.get_confidence()

    if confidence != "High":
        identified_profile_id = ""

    return identified_profile_id


def main():
    if len(sys.argv) < 4:
        print('Usage: python3 IdentifySpeaker.py <subscription_key> <identification_file_path> <force_short_audio>')
        print('\t<subscription_key> is the subscription key for the service')
        print('\t<identification_file_path> is the audio file path for identification')
        print('\t<force_short_audio> True/False waives the recommended minimum audio limit needed for enrollment')
        sys.exit('Error: Incorrect Usage.')
    subscription_key = sys.argv[1]
    identification_file_path = sys.argv[2]
    force_short_audio = sys.argv[3]

    client = IdentificationServiceHttpClientHelper.IdentificationServiceHttpClientHelper(subscription_key)

    profile_ids = print_all_profiles(client)

    identified_profile_id = identify_file(client, identification_file_path, profile_ids, force_short_audio)

    if identified_profile_id != "":
        print(identified_profile_id)

if __name__ == "__main__":
    main()
