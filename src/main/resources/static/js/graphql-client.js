async function graphqlRequest(query, variables = {}) {
    const response = await fetch("/graphql", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            query: query,
            variables: variables
        })
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(
            `Không thể kết nối GraphQL: ${response.status}`
        );
    }

    if (result.errors && result.errors.length > 0) {
        const message = result.errors
            .map(error => error.message)
            .join("\n");

        throw new Error(message);
    }

    return result.data;
}

window.graphqlRequest = graphqlRequest;